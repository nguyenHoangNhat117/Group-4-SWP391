USE HotelDB;
GO

IF OBJECT_ID('dbo.Wallet','U') IS NULL
BEGIN
    CREATE TABLE dbo.Wallet (
        WalletID   INT IDENTITY(1,1) PRIMARY KEY,
        CustomerID INT NOT NULL UNIQUE,
        Balance    DECIMAL(18,2) NOT NULL
            CONSTRAINT DF_Wallet_Balance DEFAULT 0,
        CONSTRAINT CK_Wallet_Balance CHECK (Balance = 0),
        CONSTRAINT FK_Wallet_Customer
            FOREIGN KEY (CustomerID) REFERENCES dbo.Customer(CustomerID)
    );
END
GO

IF OBJECT_ID('dbo.WalletTransaction','U') IS NULL
BEGIN
    CREATE TABLE dbo.WalletTransaction (
        TransactionID   INT IDENTITY(1,1) PRIMARY KEY,
        WalletID        INT NOT NULL,
        BookingID       INT NULL,
        Amount          DECIMAL(18,2) NOT NULL,
        TransactionType NVARCHAR(20) NOT NULL,
        Description     NVARCHAR(255) NULL,
        CreatedAt       DATETIME2 NOT NULL
            CONSTRAINT DF_WalletTransaction_CreatedAt DEFAULT SYSDATETIME(),
        CONSTRAINT CK_WalletTransaction_Amount CHECK (Amount  0),
        CONSTRAINT FK_WalletTransaction_Wallet
            FOREIGN KEY (WalletID) REFERENCES dbo.Wallet(WalletID),
        CONSTRAINT FK_WalletTransaction_Booking
            FOREIGN KEY (BookingID) REFERENCES dbo.Booking(BookingID)
    );

    CREATE UNIQUE INDEX UX_WalletTransaction_Refund_Booking
        ON dbo.WalletTransaction (BookingID)
        WHERE TransactionType = 'refund';
END
GO

INSERT INTO dbo.Wallet (CustomerID, Balance)
SELECT c.CustomerID, 0
FROM dbo.Customer c
WHERE NOT EXISTS (SELECT 1 FROM dbo.Wallet w WHERE w.CustomerID = c.CustomerID);
GO