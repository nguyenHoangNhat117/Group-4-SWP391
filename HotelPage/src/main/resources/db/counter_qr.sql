-- Chạy trong database HotelDB đang dùng, sau khi backup database.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO
IF OBJECT_ID('dbo.CounterQrCheckout','U') IS NULL
BEGIN
    CREATE TABLE dbo.CounterQrCheckout (
        OrderCode BIGINT NOT NULL PRIMARY KEY,
        BookingID INT NOT NULL REFERENCES dbo.Booking(BookingID),
        PaymentID INT NOT NULL UNIQUE REFERENCES dbo.Payment(PaymentID),
        StaffID INT NOT NULL REFERENCES dbo.[User](UserID),
        RequestKey UNIQUEIDENTIFIER NOT NULL UNIQUE,
        OriginalTotal DECIMAL(18,2) NOT NULL,
        Payable DECIMAL(18,2) NOT NULL,
        PreviouslyPaid DECIMAL(18,2) NOT NULL,
        ActualReceived BIGINT NULL,
        ExpiresEpoch BIGINT NOT NULL,
        Active BIT NOT NULL DEFAULT 1,
        State VARCHAR(20) NOT NULL DEFAULT 'pending',
        CheckoutUrl NVARCHAR(500) NULL,
        LinkAttempted BIT NOT NULL DEFAULT 0,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CheckedAt DATETIME2 NULL,
        CONSTRAINT CK_CounterQr_Order CHECK
            (OrderCode>=900000000000000 AND OrderCode<1000000000000000)
    );
    CREATE UNIQUE INDEX UX_CounterQr_ActiveBooking
        ON dbo.CounterQrCheckout(BookingID) WHERE Active=1;
END;
GO
-- Không cho sửa giá, mã giảm, khách, hạn giữ hoặc trạng thái khi QR chưa đối soát.
CREATE OR ALTER TRIGGER dbo.TR_Booking_CounterQrGuard ON dbo.Booking
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM inserted i JOIN deleted d ON d.BookingID=i.BookingID
        JOIN dbo.CounterQrCheckout q ON q.BookingID=i.BookingID AND q.Active=1
        WHERE EXISTS (
            SELECT i.TotalPrice,i.Status,i.OfflinePromotionCode,i.CustomerID,i.HoldExpiresAt
            EXCEPT
            SELECT d.TotalPrice,d.Status,d.OfflinePromotionCode,d.CustomerID,d.HoldExpiresAt
        )
    ) THROW 51001,N'QR đang chờ đối soát. Không thể đổi giá, mã giảm hoặc trạng thái booking.',1;
END;
GO
-- Chặn ghi nhận khoản thu khác trong lúc QR còn mở, kể cả từ tab cũ.
CREATE OR ALTER TRIGGER dbo.TR_Payment_CounterQrGuard ON dbo.Payment
AFTER INSERT,UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM inserted i JOIN dbo.CounterQrCheckout q
            ON q.BookingID=i.BookingID AND q.Active=1
        WHERE i.PaymentStatus='paid'
    ) THROW 51002,N'QR đang chờ đối soát. Không thể ghi nhận thêm khoản thu.',1;
END;
GO
CREATE OR ALTER PROCEDURE dbo.ExpireOfflineBookingHolds AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        ;WITH due AS (
            SELECT TOP(100) b.* FROM dbo.Booking b WITH(UPDLOCK,READPAST,ROWLOCK)
            WHERE b.CreatedByUserID IS NOT NULL AND b.Status='pending'
                AND b.HoldExpiresAt<=SYSUTCDATETIME()
                AND NOT EXISTS (
                    SELECT 1 FROM dbo.CounterQrCheckout q WITH(HOLDLOCK)
                    WHERE q.BookingID=b.BookingID AND q.Active=1
                )
            ORDER BY b.HoldExpiresAt,b.BookingID
        )
        UPDATE due SET Status='cancelled',CancellationDate=SYSDATETIME(),
            CancellationReason=N'Hết hạn thanh toán'
        WHERE NOT EXISTS (
            SELECT 1 FROM dbo.Payment p WITH(HOLDLOCK)
            WHERE p.BookingID=due.BookingID AND p.PaymentStatus='paid'
        );
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
