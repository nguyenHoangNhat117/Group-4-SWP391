-- SQL Server. Chạy trên HotelDB dùng cho dev/demo sau khi tạo schema và dữ liệu phòng.
USE [HotelDB];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

DECLARE @SampleRooms TABLE (
    RoomTypeID int PRIMARY KEY,
    RoomTypeName nvarchar(200) NOT NULL,
    RoomNumber int NULL,
    PricePerNight decimal(18, 2) NOT NULL,
    BookingMarker nvarchar(100) NOT NULL
);
DECLARE @CreatedBookings TABLE (
    BookingID int PRIMARY KEY,
    BookingMarker nvarchar(100) NOT NULL
);
DECLARE @CreatedReviews TABLE (BookingID int PRIMARY KEY);
DECLARE @UserID int, @CustomerID int, @LockResult int;
DECLARE @CheckOutDate date = DATEADD(day, -1, CONVERT(date, SYSDATETIME()));
DECLARE @CheckInDate date = DATEADD(day, -1, @CheckOutDate);

BEGIN TRY
    BEGIN TRANSACTION;

    EXEC @LockResult = sys.sp_getapplock
        @Resource = N'seed_room_type_reviews',
        @LockMode = 'Exclusive',
        @LockOwner = 'Transaction',
        @LockTimeout = 10000;
    IF @LockResult < 0
        THROW 51000, N'Không thể lấy khóa để tạo review mẫu.', 1;

    INSERT @SampleRooms (RoomTypeID, RoomTypeName, RoomNumber, PricePerNight, BookingMarker)
    SELECT rt.RoomTypeID, rt.Name, MIN(r.RoomNumber), rt.PricePerNight,
           CONCAT(N'[DEMO] Review loại phòng ', rt.RoomTypeID)
    FROM dbo.RoomType AS rt
    LEFT JOIN dbo.Room AS r ON r.RoomTypeID = rt.RoomTypeID
    GROUP BY rt.RoomTypeID, rt.Name, rt.PricePerNight;

    IF EXISTS (SELECT 1 FROM @SampleRooms WHERE RoomNumber IS NOT NULL)
    BEGIN
        SELECT @UserID = UserID
        FROM dbo.[User]
        WHERE Username = N'demo_room_type_reviews';

        IF @UserID IS NULL
        BEGIN
            INSERT dbo.[User] (Username, Password, Role, DisplayName, AccountStatus)
            VALUES (N'demo_room_type_reviews', CONVERT(nvarchar(36), NEWID()),
                    N'customer', N'Khách mẫu', N'inactive');
            SET @UserID = CONVERT(int, SCOPE_IDENTITY());
        END
        ELSE IF NOT EXISTS (
            SELECT 1 FROM dbo.[User]
            WHERE UserID = @UserID AND Role = N'customer' AND AccountStatus = N'inactive'
        )
            THROW 51001, N'Tên tài khoản demo đã được sử dụng.', 1;

        SELECT @CustomerID = MIN(CustomerID)
        FROM dbo.Customer
        WHERE UserID = @UserID AND Email = N'demo-room-type-reviews@example.invalid';

        IF @CustomerID IS NULL
        BEGIN
            INSERT dbo.Customer (FirstName, LastName, Email, UserID)
            VALUES (N'Mẫu', N'Khách', N'demo-room-type-reviews@example.invalid', @UserID);
            SET @CustomerID = CONVERT(int, SCOPE_IDENTITY());
        END;

        INSERT dbo.Booking (CustomerID, BookingDate, Status, TotalPrice, SpecialRequest)
        OUTPUT inserted.BookingID, inserted.SpecialRequest INTO @CreatedBookings
        SELECT @CustomerID, DATEADD(day, -1, @CheckInDate), N'completed',
               s.PricePerNight, s.BookingMarker
        FROM @SampleRooms AS s
        WHERE s.RoomNumber IS NOT NULL
          AND NOT EXISTS (
              SELECT 1 FROM dbo.Booking AS b
              WHERE b.CustomerID = @CustomerID AND b.SpecialRequest = s.BookingMarker
          );

        INSERT dbo.BookingDetail
            (BookingID, RoomNumber, RoomTypeID, CheckInDate, CheckOutDate, PricePerNight, GuestCount)
        SELECT b.BookingID, s.RoomNumber, s.RoomTypeID, @CheckInDate, @CheckOutDate,
               s.PricePerNight, 1
        FROM @CreatedBookings AS b
        JOIN @SampleRooms AS s ON s.BookingMarker = b.BookingMarker;

        INSERT dbo.Review (BookingID, Comment, Star, ReviewStatus)
        OUTPUT inserted.BookingID INTO @CreatedReviews
        SELECT b.BookingID,
               CASE s.RoomTypeName
                   WHEN N'Deluxe' THEN N'Giường êm, phòng sạch và cửa sổ nhìn ra thành phố rất đẹp.'
                   WHEN N'Premier' THEN N'Ban công nhìn ra biển rất thoáng. Buổi sáng ngồi uống cà phê ở đây rất dễ chịu.'
                   WHEN N'Executive' THEN N'Bàn làm việc rộng, ánh sáng tốt và phòng khá yên tĩnh.'
                   WHEN N'Family Suite' THEN N'Hai phòng ngủ rất tiện cho gia đình. Các bé có chỗ chơi ở phòng khách.'
                   WHEN N'Presidential Suite' THEN N'Sân hiên có tầm nhìn đẹp, khu vực ăn uống và phòng khách đều rộng rãi.'
                   ELSE CONCAT(N'Phòng ', s.RoomTypeName, N' sạch sẽ, giường dễ ngủ và nhận phòng nhanh.')
               END,
               CASE WHEN s.RoomTypeName IN (N'Premier', N'Presidential Suite') THEN 5.0 ELSE 4.5 END,
               N'approved'
        FROM @SampleRooms AS s
        JOIN dbo.Booking AS b
          ON b.CustomerID = @CustomerID AND b.SpecialRequest = s.BookingMarker
        WHERE s.RoomNumber IS NOT NULL AND b.Status = N'completed'
          AND NOT EXISTS (SELECT 1 FROM dbo.Review AS r WHERE r.BookingID = b.BookingID);
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT s.RoomTypeName, b.BookingID, r.ReviewID, r.Star,
       CASE
           WHEN cr.BookingID IS NOT NULL THEN N'Đã tạo review mẫu'
           WHEN r.ReviewID IS NOT NULL THEN N'Đã có review mẫu'
           WHEN s.RoomNumber IS NULL THEN N'Bỏ qua vì chưa có phòng'
           ELSE N'Không tạo được review mẫu'
       END AS SeedResult
FROM @SampleRooms AS s
LEFT JOIN dbo.Booking AS b
  ON b.CustomerID = @CustomerID AND b.SpecialRequest = s.BookingMarker
LEFT JOIN dbo.Review AS r ON r.BookingID = b.BookingID
LEFT JOIN @CreatedReviews AS cr ON cr.BookingID = b.BookingID
ORDER BY s.RoomTypeID;
