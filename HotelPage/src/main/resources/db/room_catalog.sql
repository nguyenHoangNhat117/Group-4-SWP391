-- Run in the hotel database. This migration is safe to run again.
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @types TABLE (
        LegacyName nvarchar(50), Name nvarchar(50) PRIMARY KEY, Description nvarchar(500),
        Price decimal(18,2), Beds int, Capacity int, AreaM2 int, BedLabel nvarchar(100)
    );
    INSERT @types VALUES
        (N'Standard Room', N'Deluxe City View', N'Không gian nghỉ dưỡng với thiết kế đương đại, cửa sổ kính tràn viền mang lại ánh sáng tự nhiên và tầm nhìn thoáng đãng ra thành phố.', 2500000, 1, 2, 40, N'1 Giường King (2m x 2m2)'),
        (N'Junior Suite', N'Premium Ocean View', N'Tọa lạc tại các tầng cao nhất của khách sạn, mang đến không gian yên tĩnh tuyệt đối cùng đặc quyền truy cập Club Lounge riêng biệt.', 4000000, 1, 2, 45, N'1 Giường King (2m x 2m2)'),
        (N'VIP Room', N'Executive Club Room', N'Trải nghiệm đẳng cấp thương gia với không gian làm việc rộng rãi, trang thiết bị tối tân và đặc quyền truy cập khu vực Club Lounge.', 6500000, 1, 2, 65, N'1 Giường King (2m x 2m2)'),
        (N'Family Suite', N'Grand Family Suite', N'Không gian mở hoàn hảo cho gia đình với phòng khách tách biệt và phòng ngủ riêng tư, đầy đủ tiện nghi giải trí cao cấp nhất.', 12000000, 2, 4, 95, N'2 Giường King'),
        (NULL, N'Presidential Suite (Phòng Tổng Thống)', N'Đỉnh cao của sự xa hoa, tọa lạc tại góc đẹp nhất của khách sạn với tầm nhìn Panorama 270 độ. Được thiết kế dành riêng cho giới tinh hoa, nguyên thủ quốc gia hoặc các ngôi sao hạng A.', 50000000, 1, 2, 250, N'1 Giường Super King');

    IF EXISTS (
        SELECT cfg.Name FROM @types cfg JOIN dbo.RoomType t WITH (UPDLOCK,HOLDLOCK)
            ON t.Name IN (cfg.Name, cfg.LegacyName)
        GROUP BY cfg.Name HAVING COUNT(*) > 1
    )
        THROW 51000, 'A catalog name matches more than one room type. Resolve the duplicate before migrating.', 1;

    IF EXISTS (
        SELECT 1 FROM @types cfg JOIN dbo.RoomType t ON t.Name IN (cfg.Name, cfg.LegacyName)
        JOIN dbo.Room r ON r.RoomTypeID=t.RoomTypeID
        JOIN dbo.BookingDetail d ON d.RoomNumber=r.RoomNumber
        JOIN dbo.Booking b ON b.BookingID=d.BookingID
        WHERE d.GuestCount>cfg.Capacity AND (
            b.Status='checked_in' OR (b.Status IN ('pending','confirmed')
            AND d.CheckOutDate>CAST(SYSUTCDATETIME() AT TIME ZONE 'UTC' AT TIME ZONE 'SE Asia Standard Time' AS date)))
    )
        THROW 51001, 'An active booking exceeds the new room capacity. Reassign it before migrating.', 1;

    UPDATE t SET Name=cfg.Name, Description=cfg.Description, PricePerNight=cfg.Price,
        Beds=cfg.Beds, Capacity=cfg.Capacity, IsActive=1
    FROM dbo.RoomType t JOIN @types cfg ON t.Name IN (cfg.Name, cfg.LegacyName);

    INSERT dbo.RoomType (Name,Description,PricePerNight,Beds,Capacity,Picture,IsActive)
    SELECT cfg.Name,cfg.Description,cfg.Price,cfg.Beds,cfg.Capacity,N'roomType/VipProMaxRoom.jpg',1
    FROM @types cfg
    WHERE NOT EXISTS (SELECT 1 FROM dbo.RoomType t WHERE t.Name=cfg.Name);

    UPDATE dbo.RoomType SET IsActive=0 WHERE Name IN (N'QA Family',N'QA Standard');

    IF OBJECT_ID(N'dbo.RoomTypeSpecifications',N'U') IS NULL
        CREATE TABLE dbo.RoomTypeSpecifications (
            RoomTypeID int PRIMARY KEY REFERENCES dbo.RoomType(RoomTypeID) ON DELETE CASCADE,
            AreaM2 int NOT NULL CHECK (AreaM2>0),
            BedLabel nvarchar(100) NOT NULL
        );

    UPDATE spec SET AreaM2=cfg.AreaM2, BedLabel=cfg.BedLabel
    FROM dbo.RoomTypeSpecifications spec JOIN dbo.RoomType t ON spec.RoomTypeID=t.RoomTypeID
    JOIN @types cfg ON cfg.Name=t.Name;

    INSERT dbo.RoomTypeSpecifications (RoomTypeID,AreaM2,BedLabel)
    SELECT t.RoomTypeID,cfg.AreaM2,cfg.BedLabel
    FROM dbo.RoomType t JOIN @types cfg ON cfg.Name=t.Name
    WHERE NOT EXISTS (SELECT 1 FROM dbo.RoomTypeSpecifications spec WHERE spec.RoomTypeID=t.RoomTypeID);

    INSERT dbo.Room (RoomTypeID,Status)
    SELECT t.RoomTypeID,N'available' FROM dbo.RoomType t
    WHERE t.Name=N'Presidential Suite (Phòng Tổng Thống)'
      AND NOT EXISTS (SELECT 1 FROM dbo.Room r WHERE r.RoomTypeID=t.RoomTypeID);

    DECLARE @amenities TABLE (Name nvarchar(100) PRIMARY KEY, LegacyName nvarchar(100), Tier int);
    INSERT @amenities VALUES
        (N'Smart TV 55 inch',N'TV',1),
        (N'Minibar',N'Mini Bar',1),
        (N'Két sắt điện tử',NULL,1),
        (N'Phòng tắm đứng (Walk-in shower)',NULL,1),
        (N'Wifi tốc độ cao',N'Free Wi-Fi',1),
        (N'Điều hòa trung tâm',N'Air Conditioning',1),
        (N'Bồn tắm nằm (Bathtub)',N'Bathtub',2),
        (N'Áo choàng tắm cao cấp',NULL,2),
        (N'Máy pha trà/cà phê cơ bản',NULL,2),
        (N'Máy pha cà phê',NULL,3),
        (N'Bàn làm việc giám đốc',NULL,3),
        (N'Bồn tắm ngâm có view nhìn ra ngoài trời (View Bathtub)',NULL,3),
        (N'2 Smart TV 65 inch (Phòng khách & Phòng ngủ)',NULL,4),
        (N'Bộ Sofa tiếp khách',NULL,4),
        (N'Bồn tắm sục (Jacuzzi)',NULL,4),
        (N'Tủ lạnh size lớn (Full-size Refrigerator)',NULL,4),
        (N'Bàn ăn gia đình 4 ghế',NULL,4),
        (N'Quầy Bar riêng với các loại rượu mạnh cao cấp',NULL,5),
        (N'Khu vực Bếp riêng (Private Kitchenette) trang bị đầy đủ dụng cụ',NULL,5),
        (N'Phòng xông hơi (Sauna) cá nhân',NULL,5),
        (N'Lối đi/Thang máy riêng biệt',NULL,5),
        (N'Dịch vụ Đưa đón sân bay bằng xe siêu sang (Limousine/Maybach)',NULL,5);

    UPDATE a SET Name=cfg.Name, Description=NULL
    FROM dbo.Amenity a JOIN @amenities cfg ON a.Name=cfg.LegacyName
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Amenity target WHERE target.Name=cfg.Name);

    INSERT dbo.Amenity (Name,Description)
    SELECT cfg.Name,NULL FROM @amenities cfg
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Amenity a WHERE a.Name=cfg.Name);

    DECLARE @tiers TABLE (Tier int PRIMARY KEY, Name nvarchar(50));
    INSERT @tiers VALUES (1,N'Deluxe City View'), (2,N'Premium Ocean View'), (3,N'Executive Club Room'),
        (4,N'Grand Family Suite'), (5,N'Presidential Suite (Phòng Tổng Thống)');

    DELETE link FROM dbo.RoomTypeAmenity link JOIN dbo.RoomType t ON t.RoomTypeID=link.RoomTypeID
    JOIN @tiers tier ON tier.Name=t.Name
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.Amenity a JOIN @amenities cfg ON a.Name=cfg.Name
        WHERE a.AmenityID=link.AmenityID AND (cfg.Tier=tier.Tier OR (tier.Tier<=3 AND cfg.Tier<tier.Tier))
    );

    INSERT dbo.RoomTypeAmenity (RoomTypeID,AmenityID)
    SELECT DISTINCT t.RoomTypeID,a.AmenityID
    FROM @tiers tier JOIN dbo.RoomType t ON t.Name=tier.Name
    JOIN @amenities cfg ON cfg.Tier=tier.Tier OR (tier.Tier<=3 AND cfg.Tier<tier.Tier)
    JOIN dbo.Amenity a ON a.Name=cfg.Name
    WHERE NOT EXISTS (SELECT 1 FROM dbo.RoomTypeAmenity link
        WHERE link.RoomTypeID=t.RoomTypeID AND link.AmenityID=a.AmenityID);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
