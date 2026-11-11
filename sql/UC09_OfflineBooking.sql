USE [HotelDB];
GO

SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

/* =========================================================
   1. BỔ SUNG CẤU TRÚC UC09
   ========================================================= */

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.Booking', N'U') IS NULL
        THROW 51009, N'Không tìm thấy bảng Booking.', 1;

    IF OBJECT_ID(N'dbo.Payment', N'U') IS NULL
        THROW 51009, N'Không tìm thấy bảng Payment.', 1;

    IF OBJECT_ID(N'dbo.AccountContact', N'U') IS NULL
        THROW 51009, N'Cần cập nhật SQL UC18 trước UC09.', 1;

    -- Người tạo booking offline
    IF COL_LENGTH('dbo.Booking', 'CreatedByUserID') IS NULL
        ALTER TABLE dbo.Booking
        ADD CreatedByUserID INT NULL;

    -- Chống tạo booking trùng khi gửi lại yêu cầu
    IF COL_LENGTH('dbo.Booking', 'OfflineRequestKey') IS NULL
        ALTER TABLE dbo.Booking
        ADD OfflineRequestKey UNIQUEIDENTIFIER NULL;

    -- Mã khuyến mãi được chọn trước khi thanh toán
    IF COL_LENGTH('dbo.Booking', 'OfflinePromotionCode') IS NULL
        ALTER TABLE dbo.Booking
        ADD OfflinePromotionCode NVARCHAR(50) NULL;

    -- Thời điểm hết hạn giữ phòng, lưu theo UTC
    IF COL_LENGTH('dbo.Booking', 'HoldExpiresAt') IS NULL
        ALTER TABLE dbo.Booking
        ADD HoldExpiresAt DATETIME2 NULL;

    -- Tỷ lệ cọc của booking
    IF COL_LENGTH('dbo.Booking', 'OfflineDepositPercent') IS NULL
        ALTER TABLE dbo.Booking
        ADD OfflineDepositPercent INT NULL;

    /*
       Bổ sung chính sách cho booking offline cũ còn thiếu dữ liệu.

       Nếu HoldExpiresAt đang NULL:
       cấp hạn 30 phút tính từ lúc chạy script.

       Nếu đã có hạn:
       giữ nguyên, không kéo dài.
    */
    EXEC(N'
        UPDATE dbo.Booking
        SET HoldExpiresAt = COALESCE(
                HoldExpiresAt,
                DATEADD(MINUTE, 30, SYSUTCDATETIME())
            ),
            OfflineDepositPercent = COALESCE(
                OfflineDepositPercent,
                20
            )
        WHERE CreatedByUserID IS NOT NULL;
    ');

    -- Giữ liên kết với tài khoản nhân viên tạo booking
    IF NOT EXISTS (
        SELECT 1
        FROM sys.foreign_keys
        WHERE parent_object_id = OBJECT_ID(N'dbo.Booking')
          AND name = N'FK_Booking_CreatedByUser'
    )
    BEGIN
        EXEC(N'
            ALTER TABLE dbo.Booking
            ADD CONSTRAINT FK_Booking_CreatedByUser
            FOREIGN KEY (CreatedByUserID)
            REFERENCES dbo.[User](UserID);
        ');
    END;

    -- Mỗi yêu cầu tạo offline chỉ được có một booking
    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.Booking')
          AND name = N'UX_Booking_OfflineRequestKey'
    )
    BEGIN
        EXEC(N'
            CREATE UNIQUE INDEX UX_Booking_OfflineRequestKey
            ON dbo.Booking(OfflineRequestKey)
            WHERE OfflineRequestKey IS NOT NULL;
        ');
    END;

    COMMIT TRANSACTION;

    PRINT N'Đã cập nhật cấu trúc database cho UC09.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/* =========================================================
   2. PROCEDURE HỦY BOOKING OFFLINE HẾT HẠN

   Chỉ hủy khi:
   - Là booking offline.
   - Đang pending.
   - Đã hết hạn giữ phòng.
   - Không có giao dịch thanh toán paid.

   Không hủy booking đã confirmed.
   ========================================================= */

CREATE OR ALTER PROCEDURE dbo.ExpireOfflineBookingHolds
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        ;WITH due AS (
            SELECT TOP (100) b.*
            FROM dbo.Booking b
                WITH (UPDLOCK, READPAST, ROWLOCK)
            WHERE b.CreatedByUserID IS NOT NULL
              AND b.Status = 'pending'
              AND b.HoldExpiresAt <= SYSUTCDATETIME()
            ORDER BY
                b.HoldExpiresAt,
                b.BookingID
        )
        UPDATE due
        SET Status = 'cancelled',
            CancellationDate = SYSDATETIME(),
            CancellationReason = N'Hết hạn thanh toán'
        WHERE NOT EXISTS (
            SELECT 1
            FROM dbo.Payment p WITH (HOLDLOCK)
            WHERE p.BookingID = due.BookingID
              AND p.PaymentStatus = 'paid'
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
GO

/* =========================================================
   3. CHẠY RÀ SOÁT HẾT HẠN MỘT LẦN NGAY BÂY GIỜ
   ========================================================= */

EXEC dbo.ExpireOfflineBookingHolds;
GO

/* =========================================================
   4. KIỂM TRA KẾT QUẢ
   ========================================================= */

SELECT
    DB_NAME() AS CurrentDatabase,
    COL_LENGTH(
        'dbo.Booking',
        'CreatedByUserID'
    ) AS CreatedByColumn,
    COL_LENGTH(
        'dbo.Booking',
        'HoldExpiresAt'
    ) AS HoldExpiresColumn,
    COL_LENGTH(
        'dbo.Booking',
        'OfflineDepositPercent'
    ) AS DepositColumn,
    OBJECT_ID(
        N'dbo.ExpireOfflineBookingHolds',
        N'P'
    ) AS ExpiryProcedureID;
GO

-- Hiển thị 20 booking offline mới nhất
SELECT TOP (20)
    b.BookingID,
    b.Status,
    b.CreatedByUserID,
    b.OfflineDepositPercent,
    b.HoldExpiresAt AS HanGiuPhongUTC,
    SYSUTCDATETIME() AS GioHienTaiUTC,
    DATEDIFF(
        SECOND,
        SYSUTCDATETIME(),
        b.HoldExpiresAt
    ) AS SoGiayConLai,
    b.CancellationDate,
    b.CancellationReason
FROM dbo.Booking b
WHERE b.CreatedByUserID IS NOT NULL
ORDER BY b.BookingID DESC;
GO