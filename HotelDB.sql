CREATE DATABASE HotelDB;
GO

USE HotelDB;
GO

/* ============================================================
   1. USER / AUTHENTICATION & AUTHORIZATION
   Supports Customer / Staff / Admin / Manager / IT Support.
   ============================================================ */
CREATE TABLE [User] (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    [Password] NVARCHAR(255) NOT NULL,
    [Role] NVARCHAR(20) NOT NULL
        CONSTRAINT CK_User_Role
        CHECK ([Role] IN ('customer', 'staff', 'admin', 'manager', 'it_support')),
    DisplayName NVARCHAR(100) NOT NULL,
    AccountStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_User_AccountStatus DEFAULT 'active'
        CONSTRAINT CK_User_AccountStatus
        CHECK (AccountStatus IN ('active', 'locked', 'inactive')),
    CreatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_User_CreatedAt DEFAULT SYSDATETIME()
);
GO

/* Existing sample users */
SET IDENTITY_INSERT [User] ON;

INSERT INTO [User]
    (UserID, Username, [Password], [Role], DisplayName, AccountStatus)
VALUES
    (9,    'vu',        'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Vu',             'active'),
    (10,   'np',        'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Nguyen',         'active'),
    (11,   'khang1',    'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Khang',          'active'),
    (12,   'j97',       'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Phat',           'active'),
    (13,   'phat1111',  'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Phat',           'active'),
    (14,   'nhan',      'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Nhan',           'active'),
    (15,   'wren',      'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Wren',           'active'),
    (1013, 'khang1111', 'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Khang1111',      'active'),
    (2013, 'khang2',    'e10adc3949ba59abbe56e057f20f883e', 'customer', 'Khang',          'active'),
    (1005, 'admin',     '21232f297a57a5a743894a0e4a801fc3', 'admin',    'Administrator',   'active');

SET IDENTITY_INSERT [User] OFF;
GO

/* ============================================================
   2. CUSTOMER PROFILE
   ============================================================ */
CREATE TABLE Customer (
    CustomerID INT IDENTITY(1,1) PRIMARY KEY,
    FirstName NVARCHAR(50) NULL,
    LastName NVARCHAR(50) NULL,
    Email NVARCHAR(100) NOT NULL,
    Phone NVARCHAR(20) NULL,
    UserID INT NOT NULL UNIQUE,
    Country NVARCHAR(50) NULL,
    Street NVARCHAR(100) NULL,
    City NVARCHAR(50) NULL,
    Avatar NVARCHAR(255) NULL,

    CONSTRAINT UQ_Customer_Email UNIQUE (Email),
    CONSTRAINT FK_Customer_User
        FOREIGN KEY (UserID) REFERENCES [User](UserID)
);
GO

SET IDENTITY_INSERT Customer ON;

INSERT INTO Customer
    (CustomerID, FirstName, LastName, Email, Phone, UserID, Country, Street, City, Avatar)
VALUES
    (10,   'Vu',      'Dang Hoang', 'vu@gmail.com',          '0123456789', 9,    'Vietnam',   'a',           'Can Tho',   'avatar/a-spring-afternoon-4k-v6-1920x1080.jpg'),
    (12,   'Nguyen',  'Tran',       'np1552005@gmail.com',   '0364933038', 10,   'Trung Quoc', 'Phong Dien', 'Hau Giang', NULL),
    (15,   'Khang',   'Diep Vy',    'np123@gmail.com',      NULL,         11,   NULL,         NULL,          NULL,         NULL),
    (17,   'Phat',    'Ha Thai',    'j97@gmail.com',        '0303030303', 12,   'Viet Nam',   NULL,          'Ben Tre',    'avatar/jack.jpg'),
    (18,   NULL,      NULL,         'nhan@gmail.com',       NULL,         14,   NULL,         NULL,          NULL,         NULL),
    (19,   NULL,      NULL,         'wren@gmail.com',        NULL,         15,   NULL,         NULL,          NULL,         NULL),
    (21,   NULL,      NULL,         'nguyen1111@gmail.com',  NULL,         13,   NULL,         NULL,          NULL,         NULL),
    (1018, NULL,      NULL,         'khang1111@gmail.com',   NULL,         1013, NULL,         NULL,          NULL,         NULL),
    (2018, 'Khang',   'Diep Vy',    'khang@gmail.com',       NULL,         2013, 'Vietnam',    NULL,          'Can Tho',    NULL);

SET IDENTITY_INSERT Customer OFF;
GO

/* ============================================================
   3. ROOM TYPE
   Price is stored consistently in VND.
   ============================================================ */
CREATE TABLE RoomType (
    RoomTypeID INT IDENTITY(1,1) PRIMARY KEY,
    [Name] NVARCHAR(50) NOT NULL,
    [Description] NVARCHAR(500) NULL,
    PricePerNight DECIMAL(18,2) NOT NULL,
    Beds INT NOT NULL,
    Capacity INT NOT NULL,
    Picture NVARCHAR(255) NULL,
    IsActive BIT NOT NULL
        CONSTRAINT DF_RoomType_IsActive DEFAULT 1,

    CONSTRAINT CK_RoomType_Price CHECK (PricePerNight >= 0),
    CONSTRAINT CK_RoomType_Beds CHECK (Beds > 0),
    CONSTRAINT CK_RoomType_Capacity CHECK (Capacity > 0)
);
GO

INSERT INTO RoomType
    (Name, Description, PricePerNight, Beds, Capacity, Picture)
VALUES
    ('Standard Room', 'A comfortable and affordable room ideal for solo travelers.', 59000, 1, 2, 'roomType/standard-room.jpg'),
    ('Junior Suite',  'A stylish and spacious suite ideal for couples.',             89000, 1, 2, 'roomType/junior-1.jpg'),
    ('Family Suite',  'A spacious and fully equipped suite perfect for families.',   129000, 2, 6, 'roomType/family-suite-1.jpg'),
    ('VIP Room',      'Exclusive VIP suite with luxurious amenities.',               299000, 2, 4, 'roomType/junior-3.jpg');
GO

/* ============================================================
   4. AMENITIES
   ============================================================ */
CREATE TABLE Amenity (
    AmenityID INT IDENTITY(1,1) PRIMARY KEY,
    [Name] NVARCHAR(100) NOT NULL UNIQUE,
    [Description] NVARCHAR(255) NULL
);
GO

INSERT INTO Amenity ([Name], [Description])
VALUES
    ('Free Wi-Fi', 'High-speed wireless Internet'),
    ('Air Conditioning', 'Individual air conditioning'),
    ('TV', 'Smart TV'),
    ('Mini Bar', 'Mini bar in room'),
    ('Breakfast', 'Breakfast included'),
    ('Bathtub', 'Private bathtub');
GO

CREATE TABLE RoomTypeAmenity (
    RoomTypeID INT NOT NULL,
    AmenityID INT NOT NULL,

    CONSTRAINT PK_RoomTypeAmenity PRIMARY KEY (RoomTypeID, AmenityID),
    CONSTRAINT FK_RoomTypeAmenity_RoomType
        FOREIGN KEY (RoomTypeID) REFERENCES RoomType(RoomTypeID),
    CONSTRAINT FK_RoomTypeAmenity_Amenity
        FOREIGN KEY (AmenityID) REFERENCES Amenity(AmenityID)
);
GO

INSERT INTO RoomTypeAmenity (RoomTypeID, AmenityID)
VALUES
    (1,1),(1,2),(1,3),
    (2,1),(2,2),(2,3),(2,5),
    (3,1),(3,2),(3,3),(3,4),(3,5),
    (4,1),(4,2),(4,3),(4,4),(4,5),(4,6);
GO

/* ============================================================
   5. ROOM
   Status is the physical/current room status.
   Booking availability for a date range is calculated from
   BookingDetail, not only from Room.Status.
   ============================================================ */
CREATE TABLE Room (
    RoomNumber INT IDENTITY(100,1) PRIMARY KEY,
    RoomTypeID INT NOT NULL,
    [Status] NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Room_Status DEFAULT 'available'
        CONSTRAINT CK_Room_Status
        CHECK ([Status] IN ('available', 'maintenance', 'out_of_service')),

    CONSTRAINT FK_Room_RoomType
        FOREIGN KEY (RoomTypeID) REFERENCES RoomType(RoomTypeID)
);
GO

INSERT INTO Room (RoomTypeID, [Status])
VALUES
    (1, 'available'),
    (2, 'available'),
    (3, 'available'),
    (3, 'maintenance'),
    (4, 'available'),
    (1, 'available'),
    (2, 'available'),
    (3, 'available');
GO

/* ============================================================
   6. DISCOUNT / PROMOTION
   ============================================================ */
CREATE TABLE Discount (
    DiscountID INT IDENTITY(1,1) PRIMARY KEY,
    Code NVARCHAR(50) NOT NULL UNIQUE,
    Quantity INT NOT NULL
        CONSTRAINT DF_Discount_Quantity DEFAULT 0,
    SaleOff DECIMAL(5,2) NOT NULL,
    StartDate DATE NULL,
    EndDate DATE NULL,
    MinimumAmount DECIMAL(18,2) NULL,
    MaximumDiscount DECIMAL(18,2) NULL,
    IsActive BIT NOT NULL
        CONSTRAINT DF_Discount_IsActive DEFAULT 1,

    CONSTRAINT CK_Discount_Quantity CHECK (Quantity >= 0),
    CONSTRAINT CK_Discount_SaleOff CHECK (SaleOff >= 0 AND SaleOff <= 100),
    CONSTRAINT CK_Discount_Date CHECK (EndDate IS NULL OR StartDate IS NULL OR EndDate >= StartDate),
    CONSTRAINT CK_Discount_MinimumAmount CHECK (MinimumAmount IS NULL OR MinimumAmount >= 0),
    CONSTRAINT CK_Discount_MaximumDiscount CHECK (MaximumDiscount IS NULL OR MaximumDiscount >= 0)
);
GO

INSERT INTO Discount
    (Code, Quantity, SaleOff, StartDate, EndDate, MinimumAmount, MaximumDiscount, IsActive)
VALUES
    ('giam10', 9,   10.00, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('giam15', 50,  15.00, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('giam20', 30,  20.00, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('giam25', 20,  25.00, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('giam30', 0,   30.00, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('giam5',  200,  5.00, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('giam50', 4,   50.00, '2026-01-01', '2026-12-31', NULL, NULL, 1);
GO

CREATE INDEX IX_Discount_ActiveDates
    ON Discount (IsActive, StartDate, EndDate, Code);
GO

/* ============================================================
   7. BOOKING HEADER
   One booking can contain one or many rooms.
   ============================================================ */
CREATE TABLE Booking (
    BookingID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerID INT NOT NULL,
    BookingDate DATETIME2 NOT NULL
        CONSTRAINT DF_Booking_BookingDate DEFAULT SYSDATETIME(),
    Status NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Booking_Status DEFAULT 'pending'
        CONSTRAINT CK_Booking_Status
        CHECK (Status IN (
            'pending',
            'confirmed',
            'cancelled',
            'checked_in',
            'checked_out',
            'completed'
        )),
    TotalPrice DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_Booking_TotalPrice DEFAULT 0,
    SpecialRequest NVARCHAR(500) NULL,
    CancellationDate DATETIME2 NULL,
    CancellationReason NVARCHAR(255) NULL,

    CONSTRAINT CK_Booking_TotalPrice CHECK (TotalPrice >= 0),
    CONSTRAINT CK_Booking_Cancellation
        CHECK (
            (Status = 'cancelled' AND CancellationDate IS NOT NULL)
            OR
            (Status <> 'cancelled' AND CancellationDate IS NULL)
        ),
    CONSTRAINT FK_Booking_Customer
        FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);
GO

/* ============================================================
   8. BOOKING DETAIL
   Allows one booking to contain multiple rooms.
   ============================================================ */
CREATE TABLE BookingDetail (
    BookingDetailID INT IDENTITY(1,1) PRIMARY KEY,
    BookingID INT NOT NULL,
    RoomNumber INT NOT NULL,
    CheckInDate DATE NOT NULL,
    CheckOutDate DATE NOT NULL,
    PricePerNight DECIMAL(18,2) NOT NULL,
    GuestCount INT NOT NULL
        CONSTRAINT DF_BookingDetail_GuestCount DEFAULT 1,
    SpecialRequest NVARCHAR(500) NULL,

    CONSTRAINT CK_BookingDetail_Date
        CHECK (CheckOutDate > CheckInDate),
    CONSTRAINT CK_BookingDetail_Price
        CHECK (PricePerNight >= 0),
    CONSTRAINT CK_BookingDetail_GuestCount
        CHECK (GuestCount > 0),
    CONSTRAINT FK_BookingDetail_Booking
        FOREIGN KEY (BookingID) REFERENCES Booking(BookingID),
    CONSTRAINT FK_BookingDetail_Room
        FOREIGN KEY (RoomNumber) REFERENCES Room(RoomNumber)
);
GO

/* ============================================================
   BOOKING DETAIL INTEGRITY
   - Prevents overlapping non-cancelled bookings for one room.
   - Prevents GuestCount from exceeding the room type capacity.
   These checks complement the application transaction logic.
   ============================================================ */
CREATE TRIGGER TR_BookingDetail_Validate
ON BookingDetail
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    /* Capacity validation */
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN Room r ON r.RoomNumber = i.RoomNumber
        JOIN RoomType rt ON rt.RoomTypeID = r.RoomTypeID
        WHERE i.GuestCount > rt.Capacity
    )
    BEGIN
        THROW 50001, 'Guest count exceeds room capacity.', 1;
    END;

    /* Date-overlap validation for active/non-cancelled bookings */
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN Booking ib ON ib.BookingID = i.BookingID
        JOIN BookingDetail bd
            ON bd.RoomNumber = i.RoomNumber
           AND bd.BookingDetailID <> i.BookingDetailID
           AND bd.CheckInDate < i.CheckOutDate
           AND bd.CheckOutDate > i.CheckInDate
        JOIN Booking eb ON eb.BookingID = bd.BookingID
        WHERE ib.Status <> 'cancelled'
          AND eb.Status <> 'cancelled'
    )
    BEGIN
        THROW 50002, 'Room is already booked for the selected date range.', 1;
    END;
END;
GO

CREATE INDEX IX_BookingDetail_Room_Dates
    ON BookingDetail (RoomNumber, CheckInDate, CheckOutDate);
GO

CREATE INDEX IX_Booking_Customer_Status
    ON Booking (CustomerID, Status);
GO

/* ============================================================
   9. PAYMENT
   ============================================================ */
CREATE TABLE Payment (
    PaymentID INT IDENTITY(1,1) PRIMARY KEY,
    BookingID INT NOT NULL,
    PaymentDate DATETIME2 NULL,
    Amount DECIMAL(18,2) NOT NULL,
    PaymentMethod NVARCHAR(30) NOT NULL
        CONSTRAINT CK_Payment_Method
        CHECK (PaymentMethod IN (
            'cash',
            'bank_transfer',
            'credit_card',
            'e_wallet'
        )),
    PaymentStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Payment_Status DEFAULT 'pending'
        CONSTRAINT CK_Payment_Status
        CHECK (PaymentStatus IN ('pending', 'paid', 'failed', 'refunded')),
    TransactionCode NVARCHAR(100) NULL UNIQUE,
    DiscountID INT NULL,

    CONSTRAINT CK_Payment_Amount CHECK (Amount >= 0),
    CONSTRAINT CK_Payment_Date_Status
        CHECK (PaymentStatus NOT IN ('paid', 'refunded') OR PaymentDate IS NOT NULL),
    CONSTRAINT FK_Payment_Booking
        FOREIGN KEY (BookingID) REFERENCES Booking(BookingID),
    CONSTRAINT FK_Payment_Discount
        FOREIGN KEY (DiscountID) REFERENCES Discount(DiscountID)
);
GO

CREATE INDEX IX_Payment_Booking_Status
    ON Payment (BookingID, PaymentStatus);
GO

/* ============================================================
   10. REVIEW
   ============================================================ */
CREATE TABLE Review (
    ReviewID INT IDENTITY(1,1) PRIMARY KEY,
    BookingID INT NOT NULL,
    Comment NVARCHAR(1000) NULL,
    Star DECIMAL(2,1) NOT NULL,
    ReviewDate DATETIME2 NOT NULL
        CONSTRAINT DF_Review_ReviewDate DEFAULT SYSDATETIME(),
    ReviewStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Review_Status DEFAULT 'pending'
        CONSTRAINT CK_Review_Status
        CHECK (ReviewStatus IN ('pending', 'approved', 'rejected')),

    CONSTRAINT CK_Review_Star CHECK (Star BETWEEN 1 AND 5),
    CONSTRAINT UQ_Review_Booking UNIQUE (BookingID),
    CONSTRAINT FK_Review_Booking
        FOREIGN KEY (BookingID) REFERENCES Booking(BookingID)
);
GO

/* A review is allowed only after the related stay is completed. */
CREATE TRIGGER TR_Review_ValidateCompletedBooking
ON Review
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN Booking b ON b.BookingID = i.BookingID
        WHERE b.Status <> 'completed'
    )
    BEGIN
        THROW 50003, 'A review can only be submitted for a completed booking.', 1;
    END;
END;
GO

/* ============================================================
   11. SAMPLE BOOKING DATA
   Existing booking examples are converted to Booking +
   BookingDetail.
   ============================================================ */
INSERT INTO Booking
    (CustomerID, BookingDate, Status, TotalPrice, SpecialRequest)
VALUES
    (10,   '2025-07-07T10:00:00', 'completed', 500000,  NULL),
    (12,   '2025-07-07T11:00:00', 'completed', 1600000, NULL),
    (15,   '2025-07-06T09:00:00', 'completed', 1000000, NULL),
    (2018, '2025-07-14T08:00:00', 'completed', 4000000, NULL);
GO

INSERT INTO BookingDetail
    (BookingID, RoomNumber, CheckInDate, CheckOutDate, PricePerNight, GuestCount)
VALUES
    (1, 100, '2025-07-08', '2025-07-09', 500000, 1),
    (2, 101, '2025-07-08', '2025-07-10', 800000, 2),
    (3, 102, '2025-07-07', '2025-07-09', 500000, 2),
    (4, 102, '2025-07-14', '2025-07-19', 800000, 4);
GO

/* ============================================================
   12. SAMPLE PAYMENT DATA
   ============================================================ */
INSERT INTO Payment
    (BookingID, PaymentDate, Amount, PaymentMethod, PaymentStatus, TransactionCode, DiscountID)
VALUES
    (1, '2025-07-09T12:00:00', 500000,  'cash',         'paid', 'TXN-0001', NULL),
    (2, '2025-07-10T12:00:00', 1600000, 'bank_transfer','paid', 'TXN-0002', NULL),
    (3, '2025-07-10T12:00:00', 800000,  'e_wallet',     'paid', 'TXN-0003', 3),
    (4, '2025-07-14T12:00:00', 3600000, 'credit_card',  'paid', 'TXN-0004', 1);
GO

/* ============================================================
   13. SAMPLE REVIEW DATA
   ============================================================ */
INSERT INTO Review
    (BookingID, Comment, Star, ReviewDate, ReviewStatus)
VALUES
    (3, 'Very good experience, clean room!', 5.0, '2025-07-10T15:00:00', 'approved'),
    (4, 'Room was fine but a bit noisy.',     4.5, '2025-07-20T15:00:00', 'approved');
GO