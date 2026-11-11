-- Run against the hotel database if booking contact columns are missing.
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.Booking', N'U') IS NULL
    THROW 50000, 'Select the hotel database before running this migration.', 1;

BEGIN TRANSACTION;

IF COL_LENGTH(N'dbo.Booking', N'ContactName') IS NULL
    ALTER TABLE dbo.Booking ADD ContactName NVARCHAR(120) NULL;
IF COL_LENGTH(N'dbo.Booking', N'ContactPhone') IS NULL
    ALTER TABLE dbo.Booking ADD ContactPhone NVARCHAR(32) NULL;
IF COL_LENGTH(N'dbo.Booking', N'ContactEmail') IS NULL
    ALTER TABLE dbo.Booking ADD ContactEmail NVARCHAR(254) NULL;
IF COL_LENGTH(N'dbo.Booking', N'ContactCountry') IS NULL
    ALTER TABLE dbo.Booking ADD ContactCountry NVARCHAR(80) NULL;

COMMIT TRANSACTION;
