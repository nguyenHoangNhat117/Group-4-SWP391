-- Dừng writer phiên bản mới trước khi chạy.
-- Dữ liệu mới phải tương thích với ứng dụng cũ trước khi quay lui.
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @detailId int = OBJECT_ID(N'dbo.BookingDetail', N'U');
    DECLARE @schemaBefore nvarchar(12);
    SELECT @schemaBefore = CONVERT(nvarchar(12), value)
    FROM sys.extended_properties
    WHERE class = 1 AND major_id = @detailId AND minor_id = 0
      AND name = N'BookingDetailRoomTypeSchemaBefore';

    IF @schemaBefore IS NULL OR @schemaBefore NOT IN (N'legacy', N'main')
        THROW 51108, N'Không tìm thấy dấu vết migration đặt theo loại phòng.', 1;

    IF EXISTS (SELECT 1 FROM dbo.BookingDetail WITH (TABLOCKX,HOLDLOCK) WHERE RoomNumber IS NULL)
        THROW 51109, N'Còn chi tiết chưa gán phòng. Dừng quay lui để bảo toàn các đơn.', 1;

    IF @schemaBefore = N'legacy'
    BEGIN
        EXEC sys.sp_executesql N'
            IF EXISTS (
                SELECT 1 FROM dbo.BookingDetail d JOIN dbo.Room r ON r.RoomNumber = d.RoomNumber
                WHERE d.RoomTypeID <> r.RoomTypeID
            )
                THROW 51110, N''Có loại phòng đã lưu không suy lại được từ số phòng. Dừng quay lui để giữ dữ liệu.'', 1;';
    END;

    ALTER TABLE dbo.BookingDetail DROP CONSTRAINT CK_BookingDetail_RoomOrType;

    IF @schemaBefore = N'legacy'
    BEGIN
        DROP INDEX IX_BookingDetail_TypeStay ON dbo.BookingDetail;
        ALTER TABLE dbo.BookingDetail DROP CONSTRAINT FK_BookingDetail_RoomType, FK_BookingDetail_Room;
        ALTER TABLE dbo.BookingDetail DROP COLUMN RoomTypeID;
        ALTER TABLE dbo.BookingDetail ALTER COLUMN RoomNumber int NOT NULL;
        ALTER TABLE dbo.BookingDetail WITH CHECK ADD CONSTRAINT FK_BookingDetail_Room
            FOREIGN KEY (RoomNumber) REFERENCES dbo.Room(RoomNumber);
    END;

    EXEC sys.sp_dropextendedproperty @name = N'BookingDetailRoomTypeSchemaBefore',
         @level0type = N'SCHEMA', @level0name = N'dbo',
         @level1type = N'TABLE', @level1name = N'BookingDetail';

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
