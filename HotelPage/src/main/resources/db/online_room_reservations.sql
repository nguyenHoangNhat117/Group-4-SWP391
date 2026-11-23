-- Chạy trên cơ sở dữ liệu ứng dụng sau booking_detail_room_type.sql.
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF COL_LENGTH(N'dbo.BookingDetail', N'BookedAdults') IS NULL
    ALTER TABLE dbo.BookingDetail ADD BookedAdults int NULL, BookedChildren int NULL;

IF COL_LENGTH(N'dbo.BookingDetail', N'IsProvisionalRoom') IS NULL
    ALTER TABLE dbo.BookingDetail ADD IsProvisionalRoom bit NOT NULL
        CONSTRAINT DF_BookingDetail_IsProvisionalRoom DEFAULT (0);

IF OBJECT_ID(N'dbo.CK_BookingDetail_BookedParty', N'C') IS NULL
    EXEC sys.sp_executesql N'ALTER TABLE dbo.BookingDetail WITH CHECK ADD CONSTRAINT CK_BookingDetail_BookedParty
        CHECK ((BookedAdults IS NULL AND BookedChildren IS NULL)
            OR (BookedAdults IS NOT NULL AND BookedChildren IS NOT NULL
                AND BookedAdults > 0 AND BookedChildren >= 0 AND GuestCount = BookedAdults + BookedChildren));';

IF OBJECT_ID(N'dbo.CK_BookingDetail_ProvisionalRoom', N'C') IS NULL
    EXEC sys.sp_executesql N'ALTER TABLE dbo.BookingDetail WITH CHECK ADD CONSTRAINT CK_BookingDetail_ProvisionalRoom
        CHECK (IsProvisionalRoom = 0 OR RoomNumber IS NOT NULL);';

COMMIT TRANSACTION;
