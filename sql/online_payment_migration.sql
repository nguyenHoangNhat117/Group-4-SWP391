/* Run once against HotelDB. Keep a backup before applying. */
IF COL_LENGTH('dbo.Payment', 'DiscountAmount') IS NULL
BEGIN
    ALTER TABLE dbo.Payment ADD DiscountAmount DECIMAL(18,2) NULL;
END;
GO
