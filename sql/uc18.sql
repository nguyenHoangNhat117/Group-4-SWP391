USE HotelDB;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH('dbo.User', 'AccountVersion') IS NULL
    ALTER TABLE dbo.[User] ADD AccountVersion ROWVERSION;
IF OBJECT_ID('dbo.AccountContact', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.AccountContact (
        UserID INT NOT NULL PRIMARY KEY REFERENCES dbo.[User](UserID),
        FirstName NVARCHAR(50) NULL,
        LastName NVARCHAR(50) NULL,
        Email NVARCHAR(100) NULL,
        Phone NVARCHAR(20) NULL,
        Country NVARCHAR(50) NULL,
        City NVARCHAR(50) NULL,
        Street NVARCHAR(100) NULL,
        ContactVersion ROWVERSION
    );
    CREATE UNIQUE INDEX UX_AccountContact_Email ON dbo.AccountContact(Email)
        WHERE Email IS NOT NULL;
END;
INSERT INTO dbo.AccountContact(UserID,FirstName,LastName,Email,Phone,Country,City,Street)
SELECT u.UserID,c.FirstName,c.LastName,c.Email,c.Phone,c.Country,c.City,c.Street
FROM dbo.[User] u LEFT JOIN dbo.Customer c ON c.UserID=u.UserID
WHERE NOT EXISTS(SELECT 1 FROM dbo.AccountContact p WHERE p.UserID=u.UserID);
IF EXISTS(SELECT 1 FROM dbo.AccountContact p JOIN dbo.[User] u
    ON u.Username=p.Email AND u.UserID<>p.UserID)
    THROW 51001, 'Resolve an existing username/email collision before migration.', 1;
COMMIT;
GO
-- Existing Register/Profile/Customer modules still write Customer. Keep global
-- contacts synchronized and let the unique index reject duplicate emails.
CREATE OR ALTER TRIGGER dbo.TR_UC18_CustomerContact ON dbo.Customer
AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE p SET FirstName=i.FirstName,LastName=i.LastName,Email=i.Email,
        Phone=i.Phone,Country=i.Country,City=i.City,Street=i.Street
    FROM dbo.AccountContact p JOIN inserted i ON i.UserID=p.UserID;
    INSERT dbo.AccountContact(UserID,FirstName,LastName,Email,Phone,Country,City,Street)
    SELECT i.UserID,i.FirstName,i.LastName,i.Email,i.Phone,i.Country,i.City,i.Street
    FROM inserted i WHERE NOT EXISTS(SELECT 1 FROM dbo.AccountContact p WHERE p.UserID=i.UserID);
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_UC18_ContactIdentifier ON dbo.AccountContact
AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.[User] u
        ON u.Username=i.Email AND u.UserID<>i.UserID)
        THROW 51002, 'Email conflicts with another username.', 1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_UC18_UserIdentifier ON dbo.[User]
AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(Username) AND EXISTS(SELECT 1 FROM inserted i JOIN dbo.AccountContact p
        ON p.Email=i.Username AND p.UserID<>i.UserID)
        THROW 51003, 'Username conflicts with another email.', 1;
END;
GO