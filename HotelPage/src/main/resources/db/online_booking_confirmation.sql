-- Run against existing hotel databases before deploying online confirmation.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF COL_LENGTH(N'dbo.Booking', N'HoldExpiresAt') IS NULL
    ALTER TABLE dbo.Booking ADD HoldExpiresAt DATETIME2(7) NULL;
IF COL_LENGTH(N'dbo.Booking', N'OnlineRequestKey') IS NULL
    ALTER TABLE dbo.Booking ADD OnlineRequestKey VARCHAR(36) NULL;
IF COL_LENGTH(N'dbo.Booking', N'OnlinePaymentPercent') IS NULL
    ALTER TABLE dbo.Booking ADD OnlinePaymentPercent TINYINT NULL
        CONSTRAINT CK_Booking_OnlinePaymentPercent CHECK (OnlinePaymentPercent IN (50,100));
IF COL_LENGTH(N'dbo.Booking', N'Adults') IS NULL
    ALTER TABLE dbo.Booking ADD Adults INT NULL;
IF COL_LENGTH(N'dbo.Booking', N'Children') IS NULL
    ALTER TABLE dbo.Booking ADD Children INT NULL;
IF COL_LENGTH(N'dbo.BookingDetail', N'RoomTypeID') IS NULL
    ALTER TABLE dbo.BookingDetail ADD RoomTypeID INT NULL REFERENCES dbo.RoomType(RoomTypeID);
IF COLUMNPROPERTY(OBJECT_ID(N'dbo.BookingDetail'), N'RoomNumber', N'AllowsNull') = 0
    ALTER TABLE dbo.BookingDetail ALTER COLUMN RoomNumber INT NULL;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.Booking') AND name=N'UX_Booking_OnlineRequestKey')
    EXEC(N'CREATE UNIQUE INDEX UX_Booking_OnlineRequestKey ON dbo.Booking(OnlineRequestKey) WHERE OnlineRequestKey IS NOT NULL');
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.BookingDetail') AND name=N'IX_BookingDetail_TypeStay')
    EXEC(N'CREATE INDEX IX_BookingDetail_TypeStay ON dbo.BookingDetail(RoomTypeID,CheckInDate,CheckOutDate) INCLUDE(BookingID,RoomNumber)');

COMMIT TRANSACTION;
