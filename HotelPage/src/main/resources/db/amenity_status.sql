-- Apply to existing HotelDB installations before deploying amenity management.
IF COL_LENGTH(N'dbo.Amenity', N'IsActive') IS NULL
    ALTER TABLE dbo.Amenity ADD IsActive bit NOT NULL
        CONSTRAINT DF_Amenity_IsActive DEFAULT (1) WITH VALUES;
