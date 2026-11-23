
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.BookingDetail', N'ActualGuestCount') IS NOT NULL
    BEGIN
        EXEC sys.sp_executesql N'
            IF EXISTS (
                SELECT 1 FROM dbo.BookingDetail WITH (TABLOCKX,HOLDLOCK)
                WHERE ActualGuestCount IS NOT NULL
            )
                THROW 51142, N''Đã có số khách thực tế theo phòng. Dừng quay lui để giữ dữ liệu.'', 1;

            IF OBJECT_ID(N''dbo.CK_BookingDetail_ActualGuestCount'', N''C'') IS NOT NULL
                ALTER TABLE dbo.BookingDetail DROP CONSTRAINT CK_BookingDetail_ActualGuestCount;

            ALTER TABLE dbo.BookingDetail DROP COLUMN ActualGuestCount;';
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
