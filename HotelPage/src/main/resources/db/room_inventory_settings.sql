-- Run against an existing HotelDB before deploying admin room capacity settings.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

DECLARE @RegisteredRooms int;
SELECT @RegisteredRooms = COUNT(*) FROM dbo.Room WITH (TABLOCKX, HOLDLOCK);

IF OBJECT_ID(N'dbo.RoomInventorySettings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.RoomInventorySettings (
        SettingID tinyint NOT NULL CONSTRAINT PK_RoomInventorySettings PRIMARY KEY,
        MaxRooms int NOT NULL,
        CONSTRAINT CK_RoomInventorySettings_Singleton CHECK (SettingID = 1),
        CONSTRAINT CK_RoomInventorySettings_MaxRooms CHECK (MaxRooms > 0)
    );
END;

IF NOT EXISTS (SELECT 1 FROM dbo.RoomInventorySettings WHERE SettingID = 1)
    INSERT INTO dbo.RoomInventorySettings (SettingID, MaxRooms)
    VALUES (1, CASE WHEN @RegisteredRooms > 9 THEN @RegisteredRooms ELSE 9 END);

COMMIT TRANSACTION;
