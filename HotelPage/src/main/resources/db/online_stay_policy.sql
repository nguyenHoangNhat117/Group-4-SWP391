-- Apply to the hotel database before deploying online deposit and cancellation support.
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.Booking', N'U') IS NULL OR OBJECT_ID(N'dbo.Payment', N'U') IS NULL
    THROW 50000, 'Select the hotel database before running this migration.', 1;

BEGIN TRANSACTION;

IF COL_LENGTH(N'dbo.Booking', N'OnlineDepositPolicy') IS NULL
    ALTER TABLE dbo.Booking ADD OnlineDepositPolicy BIT NOT NULL
        CONSTRAINT DF_Booking_OnlineDepositPolicy DEFAULT 0;

IF COL_LENGTH(N'dbo.Payment', N'RefundedAmount') IS NULL
    ALTER TABLE dbo.Payment ADD RefundedAmount DECIMAL(18,0) NOT NULL
        CONSTRAINT DF_Payment_RefundedAmount DEFAULT 0;

IF OBJECT_ID(N'dbo.Wallet', N'U') IS NULL
    CREATE TABLE dbo.Wallet (
        WalletID INT IDENTITY PRIMARY KEY,
        CustomerID INT NOT NULL UNIQUE REFERENCES dbo.Customer(CustomerID),
        Balance DECIMAL(18,0) NOT NULL DEFAULT 0
    );

IF OBJECT_ID(N'dbo.WalletTransaction', N'U') IS NULL
    CREATE TABLE dbo.WalletTransaction (
        WalletTransactionID INT IDENTITY PRIMARY KEY,
        WalletID INT NOT NULL REFERENCES dbo.Wallet(WalletID),
        BookingID INT NULL REFERENCES dbo.Booking(BookingID),
        Amount DECIMAL(18,0) NOT NULL,
        TransactionType NVARCHAR(30) NOT NULL,
        Description NVARCHAR(500) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );

COMMIT TRANSACTION;
