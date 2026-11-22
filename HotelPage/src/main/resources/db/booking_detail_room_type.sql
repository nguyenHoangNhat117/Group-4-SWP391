-- Chạy trên cơ sở dữ liệu ứng dụng đã chọn, trước khi phát hành mã đặt theo loại.
-- Hỗ trợ HotelDB.sql cũ và NewDB.sql trên main.
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @detailId int = OBJECT_ID(N'dbo.BookingDetail', N'U');
    IF @detailId IS NULL
        THROW 51100, N'Không tìm thấy dbo.BookingDetail trong cơ sở dữ liệu đang chọn.', 1;

    DECLARE @schemaBefore nvarchar(12);
    SELECT @schemaBefore = CONVERT(nvarchar(12), value)
    FROM sys.extended_properties
    WHERE class = 1 AND major_id = @detailId AND minor_id = 0
      AND name = N'BookingDetailRoomTypeSchemaBefore';

    IF @schemaBefore IS NOT NULL
    BEGIN
        IF @schemaBefore NOT IN (N'legacy', N'main')
           OR NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = @detailId
                          AND name = N'CK_BookingDetail_RoomOrType' AND is_disabled = 0 AND is_not_trusted = 0)
            THROW 51101, N'Metadata migration không khớp. Kiểm tra trước khi tiếp tục.', 1;
        COMMIT TRANSACTION;
        RETURN;
    END;

    DECLARE @roomNullable bit;
    SELECT @roomNullable = is_nullable
    FROM sys.columns WHERE object_id = @detailId AND name = N'RoomNumber' AND user_type_id = TYPE_ID(N'int');

    IF @roomNullable = 0 AND COL_LENGTH(N'dbo.BookingDetail', N'RoomTypeID') IS NULL
        SET @schemaBefore = N'legacy';
    ELSE IF @roomNullable = 1 AND EXISTS (
        SELECT 1 FROM sys.columns WHERE object_id = @detailId AND name = N'RoomTypeID'
          AND user_type_id = TYPE_ID(N'int') AND is_nullable = 1
    )
        SET @schemaBefore = N'main';

    IF @schemaBefore IS NULL
        THROW 51102, N'Schema BookingDetail khác hai bản đã khảo sát. Kiểm tra cột và khóa trước khi chạy.', 1;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.foreign_keys
        WHERE parent_object_id = @detailId AND name = N'FK_BookingDetail_Room'
          AND referenced_object_id = OBJECT_ID(N'dbo.Room') AND is_disabled = 0 AND is_not_trusted = 0
          AND delete_referential_action = 0 AND update_referential_action = 0
          AND is_not_for_replication = 0
    )
        THROW 51103, N'FK phòng không khớp schema đã khảo sát.', 1;

    IF @schemaBefore = N'legacy'
    BEGIN
        ALTER TABLE dbo.BookingDetail ADD RoomTypeID int NULL;
        ALTER TABLE dbo.BookingDetail DROP CONSTRAINT FK_BookingDetail_Room;
        ALTER TABLE dbo.BookingDetail ALTER COLUMN RoomNumber int NULL;
        ALTER TABLE dbo.BookingDetail WITH CHECK ADD CONSTRAINT FK_BookingDetail_Room
            FOREIGN KEY (RoomNumber) REFERENCES dbo.Room(RoomNumber);
        EXEC sys.sp_executesql N'
            ALTER TABLE dbo.BookingDetail WITH CHECK ADD CONSTRAINT FK_BookingDetail_RoomType
                FOREIGN KEY (RoomTypeID) REFERENCES dbo.RoomType(RoomTypeID);
            CREATE INDEX IX_BookingDetail_TypeStay ON dbo.BookingDetail (RoomTypeID, CheckInDate, CheckOutDate)
                INCLUDE (BookingID, RoomNumber);';
    END
    ELSE IF NOT EXISTS (
        SELECT 1
        FROM sys.foreign_keys f JOIN sys.foreign_key_columns c ON c.constraint_object_id = f.object_id
        WHERE f.parent_object_id = @detailId AND f.referenced_object_id = OBJECT_ID(N'dbo.RoomType')
          AND COL_NAME(@detailId, c.parent_column_id) = N'RoomTypeID'
          AND COL_NAME(f.referenced_object_id, c.referenced_column_id) = N'RoomTypeID'
          AND f.is_disabled = 0 AND f.is_not_trusted = 0
    ) OR NOT EXISTS (
        SELECT 1 FROM sys.indexes WHERE object_id = @detailId AND name = N'IX_BookingDetail_TypeStay'
          AND is_unique = 0 AND is_disabled = 0
    )
        THROW 51104, N'Thiếu FK hoặc index loại phòng của schema NewDB.sql.', 1;

    -- SQL động để biên dịch sau khi RoomTypeID được thêm ở schema cũ.
    EXEC sys.sp_executesql N'
        UPDATE d SET RoomTypeID = r.RoomTypeID
        FROM dbo.BookingDetail d JOIN dbo.Room r ON r.RoomNumber = d.RoomNumber
        WHERE d.RoomTypeID IS NULL;

        IF EXISTS (SELECT 1 FROM dbo.BookingDetail WHERE RoomTypeID IS NULL)
            THROW 51105, N''Có chi tiết không xác định được loại phòng. Cần kiểm tra dữ liệu.'', 1;

        IF EXISTS (
            SELECT 1 FROM dbo.BookingDetail d
            JOIN dbo.Room r ON r.RoomNumber = d.RoomNumber
            WHERE d.RoomTypeID <> r.RoomTypeID
        )
            THROW 51106, N''Có chi tiết mà loại phòng đã lưu khác với phòng được gán.'', 1;

        IF EXISTS (
            SELECT 1 FROM dbo.Booking b
            WHERE b.Status IN (''checked_in'', ''checked_out'', ''completed'')
              AND (NOT EXISTS (SELECT 1 FROM dbo.BookingDetail d WHERE d.BookingID = b.BookingID)
                   OR EXISTS (SELECT 1 FROM dbo.BookingDetail d
                              WHERE d.BookingID = b.BookingID AND d.RoomNumber IS NULL))
        )
            THROW 51107, N''Có đơn đã nhận hoặc trả nhưng thiếu chi tiết phòng. Cần kiểm tra dữ liệu.'', 1;

        -- Cho phép writer cũ ghi số phòng trong cửa sổ chuyển phiên bản.
        ALTER TABLE dbo.BookingDetail WITH CHECK ADD CONSTRAINT CK_BookingDetail_RoomOrType
            CHECK (RoomNumber IS NOT NULL OR RoomTypeID IS NOT NULL);';

    EXEC sys.sp_addextendedproperty @name = N'BookingDetailRoomTypeSchemaBefore', @value = @schemaBefore,
         @level0type = N'SCHEMA', @level0name = N'dbo',
         @level1type = N'TABLE', @level1name = N'BookingDetail';

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
