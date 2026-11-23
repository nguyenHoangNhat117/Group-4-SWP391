
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.BookingDetail', N'U') IS NULL
        THROW 51140, N'Không tìm thấy bảng dbo.BookingDetail.', 1;

    IF COL_LENGTH(N'dbo.BookingDetail', N'ActualGuestCount') IS NULL
        ALTER TABLE dbo.BookingDetail ADD ActualGuestCount int NULL;

    IF NOT EXISTS (
        SELECT 1 FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.BookingDetail')
          AND name = N'ActualGuestCount' AND user_type_id = TYPE_ID(N'int') AND is_nullable = 1
    )
        THROW 51141, N'Cột BookingDetail.ActualGuestCount phải là int và cho phép NULL.', 1;

    IF OBJECT_ID(N'dbo.CK_BookingDetail_ActualGuestCount', N'C') IS NULL
        EXEC sys.sp_executesql N'ALTER TABLE dbo.BookingDetail WITH CHECK
            ADD CONSTRAINT CK_BookingDetail_ActualGuestCount
            CHECK (ActualGuestCount BETWEEN 1 AND 1000);';

    EXEC sys.sp_executesql N'ALTER TABLE dbo.BookingDetail WITH CHECK
        CHECK CONSTRAINT CK_BookingDetail_ActualGuestCount;';

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
