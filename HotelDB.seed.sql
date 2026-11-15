-- Demo data for the schema in HotelDB.sql. Run on a development database.
-- No DELETE, reseeding or fixed identity IDs. All inserts commit together.
-- Re-running stops if seed.admin already exists; existing data is preserved.
USE [HotelDB];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET ANSI_WARNINGS ON;
SET ANSI_PADDING ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;

BEGIN TRY
 BEGIN TRANSACTION;
 IF EXISTS (SELECT 1 FROM dbo.[User] WITH (UPDLOCK,HOLDLOCK) WHERE Username LIKE N'seed.%')
    THROW 51000, 'Seed accounts already exist. No data was changed.', 1;

 DECLARE @now datetime2 = CAST(SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'SE Asia Standard Time' AS datetime2);
 DECLARE @today date = CAST(@now AS date), @utc datetime2 = SYSUTCDATETIME();
 DECLARE @password nvarchar(255) = N'pbkdf2$210000$fnouG07h0F9AWUY+R3kR9A==$jVbejcVu58eqt3n7QwCcHXWaacZlv4ECKbktVlkcmOs=';
 DECLARE @users TABLE (Username nvarchar(50), ID int);
 INSERT dbo.[User](Username,Password,Role,DisplayName,AccountStatus,CreatedAt)
 OUTPUT inserted.Username,inserted.UserID INTO @users
 SELECT username,@password,role,displayName,'active',DATEADD(day,-120,@now)
 FROM (VALUES
 (N'seed.admin',N'admin',N'Quản trị mẫu'),
 (N'seed.manager',N'manager',N'Quản lý mẫu'),
 (N'seed.staff',N'staff',N'Lễ tân mẫu'),
 (N'seed.it',N'it_support',N'Kỹ thuật mẫu'),
 (N'seed.an',N'customer',N'Nguyễn Minh An'),
 (N'seed.binh',N'customer',N'Trần Thanh Bình'),
 (N'seed.chi',N'customer',N'Lê Ngọc Chi'),
 (N'seed.dung',N'customer',N'Phạm Anh Dũng')) v(username,role,displayName);
 DECLARE @staff int=(SELECT ID FROM @users WHERE Username=N'seed.staff');
 INSERT dbo.AccountContact(UserID,FirstName,LastName,Email,Phone,Country,City,Street)
 SELECT u.ID,a.DisplayName,N'',u.Username+N'@example.com',N'090000'+RIGHT('0000'+CAST(u.ID AS varchar(10)),4),N'Việt Nam',N'Đà Nẵng',N'Địa chỉ demo'
 FROM @users u JOIN dbo.[User] a ON a.UserID=u.ID;
 DECLARE @customers TABLE (ID int, UserID int);
 INSERT dbo.Customer(FirstName,LastName,Email,Phone,UserID,Country,City,Street)
 OUTPUT inserted.CustomerID,inserted.UserID INTO @customers
 SELECT ac.FirstName,ac.LastName,ac.Email,ac.Phone,ac.UserID,ac.Country,ac.City,ac.Street
 FROM dbo.AccountContact ac JOIN @users u ON u.ID=ac.UserID JOIN dbo.[User] a ON a.UserID=u.ID WHERE a.Role='customer';
 DECLARE @an int=(SELECT c.ID FROM @customers c JOIN @users u ON u.ID=c.UserID WHERE u.Username='seed.an');
 DECLARE @binh int=(SELECT c.ID FROM @customers c JOIN @users u ON u.ID=c.UserID WHERE u.Username='seed.binh');
 DECLARE @chi int=(SELECT c.ID FROM @customers c JOIN @users u ON u.ID=c.UserID WHERE u.Username='seed.chi');
 DECLARE @dung int=(SELECT c.ID FROM @customers c JOIN @users u ON u.ID=c.UserID WHERE u.Username='seed.dung');

 DECLARE @types TABLE(ID int, Name nvarchar(50));
 INSERT dbo.RoomType(Name,Description,PricePerNight,Beds,Capacity,Picture,IsActive)
 OUTPUT inserted.RoomTypeID,inserted.Name INTO @types
 VALUES
 (N'Demo Deluxe City View',N'Phòng đôi hướng thành phố',2500000,1,2,N'roomType/deluxe-city-view.jpeg',1),
 (N'Demo Premium Ocean View',N'Phòng đôi hướng biển',4000000,1,2,N'roomType/premium-ocean-view.jpeg',1),
 (N'Demo Executive Club Room',N'Phòng thương gia',6500000,1,2,N'roomType/executive-club-room.jpeg',1),
 (N'Demo Grand Family Suite',N'Phòng gia đình',12000000,2,4,N'roomType/grand-family-suite.jpeg',1),
 (N'Demo Presidential Suite',N'Phòng tổng thống',50000000,1,2,N'roomType/presidential-suite.jpeg',1);
 INSERT dbo.RoomTypeSpecifications(RoomTypeID,AreaM2,BedLabel)
 SELECT ID,CASE WHEN Name LIKE '%Deluxe%' THEN 40 WHEN Name LIKE '%Premium%' THEN 45 WHEN Name LIKE '%Executive%' THEN 65 WHEN Name LIKE '%Family%' THEN 95 ELSE 250 END,
 CASE WHEN Name LIKE '%Family%' THEN N'2 Giường King' ELSE N'1 Giường King' END FROM @types;
 DECLARE @amenities TABLE(ID int);
 INSERT dbo.Amenity(Name,Description) OUTPUT inserted.AmenityID INTO @amenities
 VALUES(N'Demo Wi-Fi',N'Internet miễn phí'),(N'Demo Điều hòa',N'Điều hòa riêng'),(N'Demo Smart TV',N'Truyền hình thông minh'),(N'Demo Minibar',N'Tủ lạnh mini');
 INSERT dbo.RoomTypeAmenity(RoomTypeID,AmenityID) SELECT t.ID,a.ID FROM @types t CROSS JOIN @amenities a;
 DECLARE @rooms TABLE(ID int, TypeID int);
 INSERT dbo.Room(RoomTypeID,Status,NeedsCleaning) OUTPUT inserted.RoomNumber,inserted.RoomTypeID INTO @rooms
 SELECT t.ID,'available',0 FROM @types t CROSS JOIN (VALUES(1),(2),(3),(4)) n(i);
 DECLARE @r1 int=(SELECT MIN(r.ID) FROM @rooms r JOIN @types t ON t.ID=r.TypeID WHERE t.Name LIKE '%Deluxe%');
 DECLARE @r2 int=(SELECT MIN(r.ID) FROM @rooms r JOIN @types t ON t.ID=r.TypeID WHERE t.Name LIKE '%Premium%');
 DECLARE @r3 int=(SELECT MIN(r.ID) FROM @rooms r JOIN @types t ON t.ID=r.TypeID WHERE t.Name LIKE '%Family%');
 UPDATE dbo.Room SET Status='maintenance' WHERE RoomNumber=(SELECT MAX(ID) FROM @rooms);

 INSERT dbo.Discount(Code,Quantity,SaleOff,StartDate,EndDate,MinimumAmount,MaximumDiscount,IsActive)
 VALUES(N'SEED10',99,10,DATEADD(day,-90,@today),DATEADD(day,90,@today),1000000,1000000,1),
 (N'SEED_EXPIRED',10,15,DATEADD(day,-90,@today),DATEADD(day,-1,@today),1000000,2000000,1),
 (N'SEED_DISABLED',10,20,@today,DATEADD(day,30,@today),2000000,2000000,0);
 DECLARE @discount int=(SELECT DiscountID FROM dbo.Discount WHERE Code=N'SEED10');

 -- Each scenario owns a distinct date window; cancelled stays do not block rooms.
 DECLARE @scenarios TABLE (Seq int PRIMARY KEY, CustomerID int, RoomNumber int, StartOffset int, Nights int, Guests int, Status nvarchar(20), Offline bit, DiscountAmount decimal(18,2), Charge decimal(18,2));
 INSERT @scenarios VALUES
 (1,@an,@r1,-60,2,2,'completed',0,500000,0),
 (2,@binh,@r2,-30,3,2,'completed',0,0,0),
 (3,@chi,@r3,-10,2,4,'completed',1,0,300000),
 (4,@dung,@r1,-2,5,2,'checked_in',0,0,0),
 (5,@an,@r2,3,2,2,'confirmed',0,0,0),
 (6,@binh,@r1,10,2,1,'pending',0,0,0),
 (7,@chi,@r3,7,3,4,'confirmed',1,0,0),
 (8,@dung,@r2,20,2,2,'cancelled',0,0,0),
 (9,@binh,@r1,15,2,2,'cancelled',1,0,0),
 (10,@an,@r2,-4,2,2,'checked_out',1,0,0);
 DECLARE @bookings TABLE(Seq int PRIMARY KEY, ID int);
 DECLARE @seq int=1, @id int;
 WHILE @seq<=10
 BEGIN
   INSERT dbo.Booking(CustomerID,BookingDate,Status,TotalPrice,SpecialRequest,CreatedByUserID,OfflineRequestKey,OfflineDepositPercent,ContactName,ContactPhone,ContactEmail,ContactCountry,OnlineDepositPolicy,ActualCheckInAt,ActualCheckOutAt,IdentityVerifiedBy,ActualGuestCount,CancellationDate,CancellationReason)
   SELECT s.CustomerID,DATEADD(day,CASE WHEN StartOffset<0 THEN StartOffset-10 ELSE -5 END,@now),s.Status,
   s.Nights*t.PricePerNight-s.DiscountAmount+s.Charge,CONCAT(N'[SEED] Kịch bản ',s.Seq),
   CASE WHEN Offline=1 THEN @staff END,CASE WHEN Offline=1 THEN NEWID() END,CASE WHEN Offline=1 THEN 50 END,
   c.FirstName,c.Phone,c.Email,c.Country,CASE WHEN Offline=0 THEN 1 ELSE 0 END,
   CASE WHEN s.Status IN ('checked_in','checked_out','completed') THEN DATEADD(hour,14,CAST(DATEADD(day,StartOffset,@today) AS datetime2)) END,
   CASE WHEN s.Status IN ('checked_out','completed') THEN DATEADD(hour,11,CAST(DATEADD(day,StartOffset+Nights,@today) AS datetime2)) END,
   CASE WHEN s.Status IN ('checked_in','checked_out','completed') THEN @staff END,
   CASE WHEN s.Status IN ('checked_in','checked_out','completed') THEN Guests END,
   CASE WHEN s.Status='cancelled' THEN DATEADD(day,-1,@now) END,
   CASE WHEN s.Status='cancelled' THEN N'Khách thay đổi kế hoạch; hủy trước ngày nhận phòng' END
   FROM @scenarios s JOIN dbo.Customer c ON c.CustomerID=s.CustomerID JOIN dbo.Room r ON r.RoomNumber=s.RoomNumber JOIN dbo.RoomType t ON t.RoomTypeID=r.RoomTypeID WHERE Seq=@seq;
   SET @id=CONVERT(int,SCOPE_IDENTITY());
   INSERT @bookings VALUES(@seq,@id);
   INSERT dbo.BookingDetail(BookingID,RoomNumber,CheckInDate,CheckOutDate,PricePerNight,GuestCount)
   SELECT @id,s.RoomNumber,DATEADD(day,StartOffset,@today),DATEADD(day,StartOffset+Nights,@today),t.PricePerNight,Guests
   FROM @scenarios s JOIN dbo.Room r ON r.RoomNumber=s.RoomNumber JOIN dbo.RoomType t ON t.RoomTypeID=r.RoomTypeID WHERE Seq=@seq;
   SET @seq+=1;
 END;

 INSERT dbo.OfflineCharge(BookingID,Description,Amount,CreatedByUserID,RequestKey,CreatedAt)
 SELECT m.ID,N'Giặt ủi gia đình',s.Charge,@staff,NEWID(),DATEADD(hour,-8,b.ActualCheckOutAt)
 FROM @scenarios s JOIN @bookings m ON m.Seq=s.Seq JOIN dbo.Booking b ON b.BookingID=m.ID WHERE s.Charge>0;

 -- Amount is money actually received; discounts are recorded once.
 INSERT dbo.Payment(BookingID,PaymentDate,Amount,PaymentMethod,PaymentStatus,TransactionCode,DiscountID,DiscountAmount,RefundedAmount,CollectedByUserID,CounterRequestKey)
 SELECT b.BookingID,DATEADD(hour,1,b.BookingDate),
 CASE WHEN s.Seq IN (5,7) THEN b.TotalPrice/2 ELSE b.TotalPrice END,
 CASE WHEN s.Offline=1 THEN 'cash' ELSE 'bank_transfer' END,
 CASE WHEN s.Seq IN (8,9) THEN 'refunded' ELSE 'paid' END,
 CONCAT('SEED-PAY-',b.BookingID),CASE WHEN s.DiscountAmount>0 THEN @discount END,s.DiscountAmount,
 CASE WHEN s.Seq=9 THEN b.TotalPrice ELSE 0 END,
 CASE WHEN s.Offline=1 THEN @staff END,CASE WHEN s.Offline=1 THEN NEWID() END
 FROM @scenarios s JOIN @bookings m ON m.Seq=s.Seq JOIN dbo.Booking b ON b.BookingID=m.ID WHERE s.Seq<>6;
 -- Online full refunds use status='refunded', RefundedAmount=0 in BookingDAO.
 -- Split the surcharge out of the original collection to preserve chronology.
 UPDATE p SET Amount=p.Amount-300000 FROM dbo.Payment p JOIN @bookings m ON m.ID=p.BookingID WHERE m.Seq=3;
 INSERT dbo.Payment(BookingID,PaymentDate,Amount,PaymentMethod,PaymentStatus,TransactionCode,CollectedByUserID,CounterRequestKey)
 SELECT b.BookingID,b.ActualCheckOutAt,300000,'cash','paid',CONCAT('SEED-EXTRA-',b.BookingID),@staff,NEWID() FROM dbo.Booking b JOIN @bookings m ON m.ID=b.BookingID WHERE m.Seq=3;
 INSERT dbo.OfflineRefund(BookingID,Amount,Fee,Status,Method,PaidByUserID,CreatedAt,CompletedAt)
 SELECT b.BookingID,b.TotalPrice,0,'completed','cash',@staff,DATEADD(hour,-7,b.CancellationDate),DATEADD(minute,10,DATEADD(hour,-7,b.CancellationDate)) FROM dbo.Booking b JOIN @bookings m ON m.ID=b.BookingID WHERE m.Seq=9;

 INSERT dbo.Wallet(CustomerID,Balance) SELECT ID,CASE WHEN ID=@dung THEN 8000000 ELSE 0 END FROM @customers;
 INSERT dbo.WalletTransaction(WalletID,BookingID,Amount,TransactionType,Description,CreatedAt)
 SELECT w.WalletID,b.BookingID,8000000,'refund',N'Hoàn tiền hủy đặt phòng mẫu',b.CancellationDate
 FROM dbo.Wallet w JOIN dbo.Booking b ON b.CustomerID=w.CustomerID JOIN @bookings m ON m.ID=b.BookingID WHERE m.Seq=8;
 INSERT dbo.WithdrawalRequest(CustomerID,Amount,BankName,AccountNumber,AccountHolder,Status,CreatedAt)
 VALUES(@dung,2000000,N'DEMO BANK',N'DEMO-000001',N'PHAM ANH DUNG','pending',@utc);
 -- Pending withdrawals reserve availability in the DAO, not the stored balance.
 INSERT dbo.Review(BookingID,Comment,Star,ReviewDate,ReviewStatus)
 SELECT b.BookingID,CASE m.Seq WHEN 1 THEN N'Phòng sạch, nhận phòng nhanh.' WHEN 2 THEN N'Hướng biển đẹp, nhân viên nhiệt tình.' ELSE N'Phòng rộng, phù hợp gia đình.' END,
 CASE m.Seq WHEN 1 THEN 5 WHEN 2 THEN 4 ELSE 5 END,DATEADD(hour,3,b.ActualCheckOutAt),CASE WHEN m.Seq=3 THEN 'pending' ELSE 'approved' END
 FROM dbo.Booking b JOIN @bookings m ON m.ID=b.BookingID WHERE m.Seq IN (1,2,3);
 INSERT dbo.OfflineBookingEvent(BookingID,ActorUserID,RequestKey,Action,Note,Amount,CreatedAt)
 SELECT p.BookingID,@staff,NEWID(),'payment',N'Thu tiền mặt mẫu',p.Amount,DATEADD(hour,-7,p.PaymentDate)
 FROM dbo.Payment p JOIN @bookings m ON m.ID=p.BookingID JOIN @scenarios s ON s.Seq=m.Seq WHERE s.Offline=1;
 INSERT dbo.OfflineBookingEvent(BookingID,ActorUserID,RequestKey,Action,Note,Amount,CreatedAt)
 SELECT b.BookingID,@staff,NEWID(),v.Action,N'Lịch sử thao tác mẫu',0,DATEADD(hour,-7,CASE WHEN v.Action='check_in' THEN b.ActualCheckInAt ELSE b.ActualCheckOutAt END)
 FROM dbo.Booking b JOIN @bookings m ON m.ID=b.BookingID JOIN @scenarios s ON s.Seq=m.Seq CROSS JOIN (VALUES('check_in'),('check_out')) v(Action)
 WHERE s.Offline=1 AND b.ActualCheckOutAt IS NOT NULL;
 INSERT dbo.OfflineBookingEvent(BookingID,ActorUserID,RequestKey,Action,Note,Amount,CreatedAt)
 SELECT f.BookingID,@staff,NEWID(),v.Action,N'Hủy và hoàn tiền mẫu',f.Amount,CASE WHEN v.Action='cancel' THEN f.CreatedAt ELSE f.CompletedAt END
 FROM dbo.OfflineRefund f JOIN @bookings m ON m.ID=f.BookingID CROSS JOIN (VALUES('cancel'),('refund')) v(Action);
 INSERT dbo.RoomServiceEvent(RoomNumber,ActorUserID,CreatedAt)
 SELECT d.RoomNumber,@staff,DATEADD(hour,-6,b.ActualCheckOutAt) FROM dbo.Booking b JOIN @bookings m ON m.ID=b.BookingID JOIN dbo.BookingDetail d ON d.BookingID=b.BookingID WHERE m.Seq IN (1,2,3);
 UPDATE dbo.Room SET NeedsCleaning=1 WHERE RoomNumber=@r2;

 -- Business invariants beyond FK/CHECK constraints. Any error rolls everything back.
 IF EXISTS(SELECT 1 FROM @bookings m JOIN dbo.Booking b ON b.BookingID=m.ID JOIN @scenarios s ON s.Seq=m.Seq
 CROSS APPLY(SELECT SUM(DATEDIFF(day,CheckInDate,CheckOutDate)*PricePerNight) Gross FROM dbo.BookingDetail WHERE BookingID=b.BookingID) d
 WHERE b.TotalPrice<>d.Gross-s.DiscountAmount+s.Charge)
 THROW 51001, 'Booking total mismatch.',1;
 IF EXISTS(SELECT 1 FROM dbo.BookingDetail d JOIN @bookings m ON m.ID=d.BookingID JOIN dbo.Room r ON r.RoomNumber=d.RoomNumber JOIN dbo.RoomType t ON t.RoomTypeID=r.RoomTypeID WHERE d.GuestCount>t.Capacity)
 THROW 51002, 'Guest count exceeds capacity.',1;
 IF EXISTS(SELECT 1 FROM dbo.BookingDetail a JOIN @bookings m ON m.ID=a.BookingID JOIN dbo.Booking ba ON ba.BookingID=a.BookingID
 JOIN dbo.BookingDetail d ON a.RoomNumber=d.RoomNumber AND a.BookingID<>d.BookingID AND a.CheckInDate<d.CheckOutDate AND d.CheckInDate<a.CheckOutDate
 JOIN dbo.Booking bd ON bd.BookingID=d.BookingID WHERE ba.Status<>'cancelled' AND bd.Status<>'cancelled')
 THROW 51003, 'Room dates overlap.',1;
 IF EXISTS(SELECT 1 FROM dbo.Booking b JOIN @bookings m ON m.ID=b.BookingID
 CROSS APPLY(SELECT COALESCE(SUM(Amount-RefundedAmount),0) Paid FROM dbo.Payment WHERE BookingID=b.BookingID AND PaymentStatus='paid') p
 WHERE b.Status IN ('completed','checked_out','checked_in') AND p.Paid<>b.TotalPrice)
 THROW 51004, 'Stay payment mismatch.',1;
 IF EXISTS(SELECT 1 FROM dbo.Wallet w JOIN @customers c ON c.ID=w.CustomerID
 OUTER APPLY(SELECT COALESCE(SUM(Amount),0) Balance FROM dbo.WalletTransaction WHERE WalletID=w.WalletID) t WHERE w.Balance<>t.Balance)
 THROW 51005, 'Wallet ledger mismatch.',1;
 COMMIT TRANSACTION;
 SELECT m.Seq,b.BookingID,b.Status,c.Email,b.TotalPrice,d.RoomNumber,d.CheckInDate,d.CheckOutDate
 FROM @bookings m JOIN dbo.Booking b ON b.BookingID=m.ID JOIN dbo.Customer c ON c.CustomerID=b.CustomerID JOIN dbo.BookingDetail d ON d.BookingID=b.BookingID ORDER BY m.Seq;
 PRINT N'Seed complete. Demo password: HotelDemo@123';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO
