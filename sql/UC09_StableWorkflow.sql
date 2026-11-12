/* Run AFTER script_fixed.sql (fresh database), or existing UC18 + UC09 migrations.
   Select the intended database in SSMS first. This script preserves existing rows.
   Back up the database and stop application writes during migration. */
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
IF OBJECT_ID('dbo.AccountContact') IS NULL OR COL_LENGTH('dbo.Booking','HoldExpiresAt') IS NULL
    THROW 51009, N'Run UC18 and UC09 migrations first.', 1;
GO
BEGIN TRY
 BEGIN TRANSACTION;
 IF COL_LENGTH('dbo.Room','NeedsCleaning') IS NULL
   ALTER TABLE dbo.Room ADD NeedsCleaning BIT NOT NULL CONSTRAINT DF_Room_NeedsCleaning DEFAULT 0;
 IF COL_LENGTH('dbo.Payment','CollectedByUserID') IS NULL
   ALTER TABLE dbo.Payment ADD CollectedByUserID INT NULL REFERENCES dbo.[User](UserID);
 IF COL_LENGTH('dbo.Payment','CounterRequestKey') IS NULL
   ALTER TABLE dbo.Payment ADD CounterRequestKey UNIQUEIDENTIFIER NULL;
 IF COL_LENGTH('dbo.Payment','CounterReference') IS NULL
   ALTER TABLE dbo.Payment ADD CounterReference NVARCHAR(100) NULL;
 IF COL_LENGTH('dbo.Payment','RefundedAmount') IS NULL
   ALTER TABLE dbo.Payment ADD RefundedAmount DECIMAL(18,0) NOT NULL CONSTRAINT DF_Payment_OfflineRefunded DEFAULT 0;
 IF COL_LENGTH('dbo.Payment','DiscountAmount') IS NULL
   ALTER TABLE dbo.Payment ADD DiscountAmount DECIMAL(18,2) NULL;
 IF COL_LENGTH('dbo.Booking','ActualCheckInAt') IS NULL
   ALTER TABLE dbo.Booking ADD ActualCheckInAt DATETIME2 NULL, ActualCheckOutAt DATETIME2 NULL,
       IdentityVerifiedBy INT NULL REFERENCES dbo.[User](UserID), ActualGuestCount INT NULL;

 /* Guest profiles do not require a website account. Keep uniqueness for linked accounts. */
 DECLARE @constraint SYSNAME, @ddl NVARCHAR(MAX);
 DECLARE uq CURSOR LOCAL FAST_FORWARD FOR
   SELECT kc.name FROM sys.key_constraints kc
   JOIN sys.index_columns ic ON ic.object_id=kc.parent_object_id AND ic.index_id=kc.unique_index_id
   JOIN sys.columns c ON c.object_id=ic.object_id AND c.column_id=ic.column_id
   WHERE kc.parent_object_id=OBJECT_ID('dbo.Customer') AND kc.type='UQ' AND c.name IN ('UserID','Email')
     AND (SELECT COUNT(*) FROM sys.index_columns x WHERE x.object_id=ic.object_id AND x.index_id=ic.index_id AND x.key_ordinal>0)=1;
 OPEN uq; FETCH NEXT FROM uq INTO @constraint;
 WHILE @@FETCH_STATUS=0 BEGIN
   SET @ddl=N'ALTER TABLE dbo.Customer DROP CONSTRAINT '+QUOTENAME(@constraint);
   EXEC sp_executesql @ddl; FETCH NEXT FROM uq INTO @constraint;
 END;
 CLOSE uq; DEALLOCATE uq;
 IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.Customer') AND name='UserID' AND is_nullable=0)
   ALTER TABLE dbo.Customer ALTER COLUMN UserID INT NULL;
 IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.Customer') AND name='Email' AND is_nullable=0)
   ALTER TABLE dbo.Customer ALTER COLUMN Email NVARCHAR(100) NULL;
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.Customer') AND name='UX_Customer_LinkedUser')
   CREATE UNIQUE INDEX UX_Customer_LinkedUser ON dbo.Customer(UserID) WHERE UserID IS NOT NULL;
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.Customer') AND name='UX_Customer_Email')
   CREATE UNIQUE INDEX UX_Customer_Email ON dbo.Customer(Email) WHERE Email IS NOT NULL;

 IF OBJECT_ID('dbo.OfflineBookingEvent') IS NULL
 CREATE TABLE dbo.OfflineBookingEvent (
   EventID BIGINT IDENTITY PRIMARY KEY,
   BookingID INT NOT NULL REFERENCES dbo.Booking(BookingID),
   ActorUserID INT NOT NULL REFERENCES dbo.[User](UserID),
   RequestKey UNIQUEIDENTIFIER NOT NULL UNIQUE,
   Action NVARCHAR(30) NOT NULL, Note NVARCHAR(500) NULL,
   Amount DECIMAL(18,2) NOT NULL DEFAULT 0,
   CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
 );
 IF OBJECT_ID('dbo.OfflineRefund') IS NULL
 CREATE TABLE dbo.OfflineRefund (
   RefundID INT IDENTITY PRIMARY KEY,
   BookingID INT NOT NULL UNIQUE REFERENCES dbo.Booking(BookingID),
   Amount DECIMAL(18,2) NOT NULL CHECK(Amount>0),
   Fee DECIMAL(18,2) NOT NULL CHECK(Fee>=0),
   Status NVARCHAR(20) NOT NULL DEFAULT 'pending' CHECK(Status IN ('pending','completed')),
   Method NVARCHAR(30) NULL CHECK(Method IN ('cash','bank_transfer')),
   Reference NVARCHAR(100) NULL, PaidByUserID INT NULL REFERENCES dbo.[User](UserID),
   CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(), CompletedAt DATETIME2 NULL
 );
 IF OBJECT_ID('dbo.OfflineCharge') IS NULL
 CREATE TABLE dbo.OfflineCharge (
   ChargeID INT IDENTITY PRIMARY KEY,
   BookingID INT NOT NULL REFERENCES dbo.Booking(BookingID),
   Description NVARCHAR(500) NOT NULL,
   Amount DECIMAL(18,2) NOT NULL CHECK(Amount>0),
   CreatedByUserID INT NOT NULL REFERENCES dbo.[User](UserID),
   RequestKey UNIQUEIDENTIFIER NOT NULL UNIQUE,
   CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
 );
 IF OBJECT_ID('dbo.RoomServiceEvent') IS NULL
 CREATE TABLE dbo.RoomServiceEvent (
   EventID BIGINT IDENTITY PRIMARY KEY,
   RoomNumber INT NOT NULL REFERENCES dbo.Room(RoomNumber),
   ActorUserID INT NOT NULL REFERENCES dbo.[User](UserID),
   CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
 );
 COMMIT;
END TRY
BEGIN CATCH
 IF XACT_STATE()<>0 ROLLBACK;
 THROW;
END CATCH;
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.Payment') AND name='UX_Payment_CounterRequest')
 CREATE UNIQUE INDEX UX_Payment_CounterRequest ON dbo.Payment(CounterRequestKey) WHERE CounterRequestKey IS NOT NULL;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.Payment') AND name='UX_Payment_CounterBankReference')
 CREATE UNIQUE INDEX UX_Payment_CounterBankReference ON dbo.Payment(CounterReference)
 WHERE CounterReference IS NOT NULL AND PaymentMethod='bank_transfer';
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.OfflineRefund') AND name='UX_OfflineRefund_BankReference')
 CREATE UNIQUE INDEX UX_OfflineRefund_BankReference ON dbo.OfflineRefund(Reference)
 WHERE Reference IS NOT NULL AND Method='bank_transfer' AND Status='completed';
GO
