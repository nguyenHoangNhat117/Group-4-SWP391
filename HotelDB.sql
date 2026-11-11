-- Khởi tạo HotelDB trên SQL Server. Chạy toàn bộ file trong SSMS.
-- Nếu muốn tạo một cơ sở dữ liệu khác, thay mọi chỗ có HotelDB bằng tên mới.
USE [master];
GO

IF DB_ID(N'HotelDB') IS NULL
    EXEC (N'CREATE DATABASE [HotelDB];');
GO

USE [HotelDB];
GO

IF DB_NAME() <> N'HotelDB'
    THROW 50001, N'Không thể mở HotelDB. Hãy kiểm tra quyền và lỗi tạo cơ sở dữ liệu ở trên.', 1;

IF EXISTS (
    SELECT 1
    FROM sys.objects
    WHERE schema_id = SCHEMA_ID(N'dbo')
      AND name IN (
          N'AccountContact', N'Amenity', N'Booking', N'BookingDetail', N'Customer', N'Discount', N'Payment',
          N'Review', N'Room', N'RoomType', N'RoomTypeAmenity', N'User', N'Wallet', N'WalletTransaction', N'ExpireOfflineBookingHolds'
      )
)
    THROW 50002, N'HotelDB đã có đối tượng trùng tên. Hãy dùng cơ sở dữ liệu trống hoặc đổi tên đích trong script.', 1;

SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;

BEGIN TRY
    BEGIN TRANSACTION;

/****** Object:  Table [dbo].[AccountContact]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[AccountContact](
	[UserID] [int] NOT NULL,
	[FirstName] [nvarchar](50) NULL,
	[LastName] [nvarchar](50) NULL,
	[Email] [nvarchar](100) NULL,
	[Phone] [nvarchar](20) NULL,
	[Country] [nvarchar](50) NULL,
	[City] [nvarchar](50) NULL,
	[Street] [nvarchar](100) NULL,
	[ContactVersion] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[UserID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Amenity]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Amenity](
	[AmenityID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](100) NOT NULL,
	[Description] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[AmenityID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Booking]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Booking](
	[BookingID] [int] IDENTITY(1,1) NOT NULL,
	[CustomerID] [int] NOT NULL,
	[BookingDate] [datetime2](7) NOT NULL,
	[Status] [nvarchar](20) NOT NULL,
	[TotalPrice] [decimal](18, 2) NOT NULL,
	[SpecialRequest] [nvarchar](500) NULL,
	[CancellationDate] [datetime2](7) NULL,
	[CancellationReason] [nvarchar](255) NULL,
	[CreatedByUserID] [int] NULL,
	[OfflineRequestKey] [uniqueidentifier] NULL,
	[OfflinePromotionCode] [nvarchar](50) NULL,
	[HoldExpiresAt] [datetime2](7) NULL,
	[OfflineDepositPercent] [int] NULL,
	[ContactName] [nvarchar](120) NULL,
	[ContactPhone] [nvarchar](32) NULL,
	[ContactEmail] [nvarchar](254) NULL,
	[ContactCountry] [nvarchar](80) NULL,
	[OnlineDepositPolicy] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[BookingID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[BookingDetail]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[BookingDetail](
	[BookingDetailID] [int] IDENTITY(1,1) NOT NULL,
	[BookingID] [int] NOT NULL,
	[RoomNumber] [int] NOT NULL,
	[CheckInDate] [date] NOT NULL,
	[CheckOutDate] [date] NOT NULL,
	[PricePerNight] [decimal](18, 2) NOT NULL,
	[GuestCount] [int] NOT NULL,
	[SpecialRequest] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[BookingDetailID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Customer]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Customer](
	[CustomerID] [int] IDENTITY(1,1) NOT NULL,
	[FirstName] [nvarchar](50) NULL,
	[LastName] [nvarchar](50) NULL,
	[Email] [nvarchar](100) NULL,
	[Phone] [nvarchar](20) NULL,
	[UserID] [int] NOT NULL,
	[Country] [nvarchar](50) NULL,
	[Street] [nvarchar](100) NULL,
	[City] [nvarchar](50) NULL,
	[Avatar] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[CustomerID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Discount]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Discount](
	[DiscountID] [int] IDENTITY(1,1) NOT NULL,
	[Code] [nvarchar](50) NOT NULL,
	[Quantity] [int] NOT NULL,
	[SaleOff] [decimal](5, 2) NOT NULL,
	[StartDate] [date] NULL,
	[EndDate] [date] NULL,
	[MinimumAmount] [decimal](18, 2) NULL,
	[MaximumDiscount] [decimal](18, 2) NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[DiscountID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Payment]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Payment](
	[PaymentID] [int] IDENTITY(1,1) NOT NULL,
	[BookingID] [int] NOT NULL,
	[PaymentDate] [datetime2](7) NULL,
	[Amount] [decimal](18, 2) NOT NULL,
	[PaymentMethod] [nvarchar](30) NOT NULL,
	[PaymentStatus] [nvarchar](20) NOT NULL,
	[TransactionCode] [nvarchar](100) NULL,
	[DiscountID] [int] NULL,
	[DiscountAmount] [decimal](18, 2) NULL,
	[RefundedAmount] [decimal](18, 0) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[PaymentID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Review]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Review](
	[ReviewID] [int] IDENTITY(1,1) NOT NULL,
	[BookingID] [int] NOT NULL,
	[Comment] [nvarchar](1000) NULL,
	[Star] [decimal](2, 1) NOT NULL,
	[ReviewDate] [datetime2](7) NOT NULL,
	[ReviewStatus] [nvarchar](20) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ReviewID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Room]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Room](
	[RoomNumber] [int] IDENTITY(100,1) NOT NULL,
	[RoomTypeID] [int] NOT NULL,
	[Status] [nvarchar](20) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[RoomNumber] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[RoomType]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[RoomType](
	[RoomTypeID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[Description] [nvarchar](500) NULL,
	[PricePerNight] [decimal](18, 2) NOT NULL,
	[Beds] [int] NOT NULL,
	[Capacity] [int] NOT NULL,
	[Picture] [nvarchar](255) NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[RoomTypeID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[RoomTypeAmenity]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[RoomTypeAmenity](
	[RoomTypeID] [int] NOT NULL,
	[AmenityID] [int] NOT NULL,
 CONSTRAINT [PK_RoomTypeAmenity] PRIMARY KEY CLUSTERED 
(
	[RoomTypeID] ASC,
	[AmenityID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[User]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[User](
	[UserID] [int] IDENTITY(1,1) NOT NULL,
	[Username] [nvarchar](50) NOT NULL,
	[Password] [nvarchar](255) NOT NULL,
	[Role] [nvarchar](20) NOT NULL,
	[DisplayName] [nvarchar](100) NOT NULL,
	[AccountStatus] [nvarchar](20) NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[AccountVersion] [timestamp] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[UserID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[Wallet]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[Wallet](
	[WalletID] [int] IDENTITY(1,1) NOT NULL,
	[CustomerID] [int] NOT NULL,
	[Balance] [decimal](18, 0) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[WalletID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

/****** Object:  Table [dbo].[WalletTransaction]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE TABLE [dbo].[WalletTransaction](
	[WalletTransactionID] [int] IDENTITY(1,1) NOT NULL,
	[WalletID] [int] NOT NULL,
	[BookingID] [int] NULL,
	[Amount] [decimal](18, 0) NOT NULL,
	[TransactionType] [nvarchar](30) NOT NULL,
	[Description] [nvarchar](500) NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[WalletTransactionID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (9, N'Vu', N'Dang Hoang', N'vu@gmail.com', N'0123456789', N'Vietnam', N'Can Tho', N'a')
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (10, N'Nguyen', N'Tran', N'np1552005@gmail.com', N'0364933038', N'Trung Quoc', N'Hau Giang', N'Phong Dien')
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (11, N'Khang', N'Diep Vy', N'np123@gmail.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (12, N'Phat', N'Ha Thai', N'j97@gmail.com', N'0303030303', N'Viet Nam', N'Ben Tre', NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (13, NULL, NULL, N'nguyen1111@gmail.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (14, NULL, NULL, N'nhan@gmail.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (15, NULL, NULL, N'wren@gmail.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (1005, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (1013, NULL, NULL, N'khang1111@gmail.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (2013, N'Khang', N'Diep Vy', N'khang@gmail.com', NULL, N'Vietnam', N'Can Tho', NULL)

SET IDENTITY_INSERT [dbo].[Amenity] ON 

INSERT [dbo].[Amenity] ([AmenityID], [Name], [Description]) VALUES (1, N'Free Wi-Fi', N'High-speed wireless Internet')
INSERT [dbo].[Amenity] ([AmenityID], [Name], [Description]) VALUES (2, N'Air Conditioning', N'Individual air conditioning')
INSERT [dbo].[Amenity] ([AmenityID], [Name], [Description]) VALUES (3, N'TV', N'Smart TV')
INSERT [dbo].[Amenity] ([AmenityID], [Name], [Description]) VALUES (4, N'Mini Bar', N'Mini bar in room')
INSERT [dbo].[Amenity] ([AmenityID], [Name], [Description]) VALUES (5, N'Breakfast', N'Breakfast included')
INSERT [dbo].[Amenity] ([AmenityID], [Name], [Description]) VALUES (6, N'Bathtub', N'Private bathtub')
SET IDENTITY_INSERT [dbo].[Amenity] OFF

SET IDENTITY_INSERT [dbo].[Booking] ON 

INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (1, 10, CAST(N'2025-07-07T10:00:00.0000000' AS DateTime2), N'completed', CAST(500000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (2, 12, CAST(N'2025-07-07T11:00:00.0000000' AS DateTime2), N'completed', CAST(1600000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (3, 15, CAST(N'2025-07-06T09:00:00.0000000' AS DateTime2), N'completed', CAST(1000000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (4, 2018, CAST(N'2025-07-14T08:00:00.0000000' AS DateTime2), N'completed', CAST(4000000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (6, 10, CAST(N'2026-11-10T18:52:38.0825490' AS DateTime2), N'cancelled', CAST(59000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-10T19:26:05.8713483' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (7, 10, CAST(N'2026-11-10T18:59:21.8909850' AS DateTime2), N'cancelled', CAST(59000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-10T19:40:19.2734297' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (8, 10, CAST(N'2026-11-10T19:00:38.8690496' AS DateTime2), N'cancelled', CAST(59000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-10T19:40:19.2734297' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (9, 10, CAST(N'2026-11-10T19:06:27.7235866' AS DateTime2), N'cancelled', CAST(59000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-10T19:40:19.2734297' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (10, 10, CAST(N'2026-11-10T19:26:18.1424849' AS DateTime2), N'pending', CAST(59000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (11, 10, CAST(N'2026-11-10T19:40:29.9721278' AS DateTime2), N'cancelled', CAST(2950.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-10T19:48:36.8602776' AS DateTime2), N'Cancelled by customer', NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (12, 10, CAST(N'2026-11-10T19:41:51.9300908' AS DateTime2), N'cancelled', CAST(472000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-10T19:53:27.5014046' AS DateTime2), N'Cancelled by customer', NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy]) VALUES (13, 10, CAST(N'2026-11-10T19:52:35.8209519' AS DateTime2), N'pending', CAST(59000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, N'Dang Hoang Vu', N'0123456789', N'vu@gmail.com', N'Vietnam', 1)
SET IDENTITY_INSERT [dbo].[Booking] OFF

SET IDENTITY_INSERT [dbo].[BookingDetail] ON 

INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (1, 1, 100, CAST(N'2025-07-08' AS Date), CAST(N'2025-07-09' AS Date), CAST(500000.00 AS Decimal(18, 2)), 1, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (2, 2, 101, CAST(N'2025-07-08' AS Date), CAST(N'2025-07-10' AS Date), CAST(800000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (3, 3, 102, CAST(N'2025-07-07' AS Date), CAST(N'2025-07-09' AS Date), CAST(500000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (4, 4, 101, CAST(N'2025-07-14' AS Date), CAST(N'2025-07-19' AS Date), CAST(800000.00 AS Decimal(18, 2)), 4, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (5, 6, 100, CAST(N'2026-12-01' AS Date), CAST(N'2026-12-02' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (6, 7, 100, CAST(N'2026-12-03' AS Date), CAST(N'2026-12-04' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (7, 8, 105, CAST(N'2026-12-01' AS Date), CAST(N'2026-12-02' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (8, 9, 100, CAST(N'2027-01-05' AS Date), CAST(N'2027-01-06' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (9, 10, 100, CAST(N'2026-12-21' AS Date), CAST(N'2026-12-22' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (10, 11, 100, CAST(N'2026-12-30' AS Date), CAST(N'2026-12-31' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (11, 12, 100, CAST(N'2026-11-12' AS Date), CAST(N'2026-11-20' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest]) VALUES (12, 13, 100, CAST(N'2026-12-14' AS Date), CAST(N'2026-12-15' AS Date), CAST(59000.00 AS Decimal(18, 2)), 2, NULL)
SET IDENTITY_INSERT [dbo].[BookingDetail] OFF

SET IDENTITY_INSERT [dbo].[Customer] ON 

INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (10, N'Vu', N'Dang Hoang', N'vu@gmail.com', N'0123456789', 9, N'Vietnam', N'a', N'Can Tho', N'avatar/a-spring-afternoon-4k-v6-1920x1080.jpg')
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (12, N'Nguyen', N'Tran', N'np1552005@gmail.com', N'0364933038', 10, N'Trung Quoc', N'Phong Dien', N'Hau Giang', NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (15, N'Khang', N'Diep Vy', N'np123@gmail.com', NULL, 11, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (17, N'Phat', N'Ha Thai', N'j97@gmail.com', N'0303030303', 12, N'Viet Nam', NULL, N'Ben Tre', N'avatar/jack.jpg')
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (18, NULL, NULL, N'nhan@gmail.com', NULL, 14, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (19, NULL, NULL, N'wren@gmail.com', NULL, 15, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (21, NULL, NULL, N'nguyen1111@gmail.com', NULL, 13, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (1018, NULL, NULL, N'khang1111@gmail.com', NULL, 1013, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (2018, N'Khang', N'Diep Vy', N'khang@gmail.com', NULL, 2013, N'Vietnam', NULL, N'Can Tho', NULL)
SET IDENTITY_INSERT [dbo].[Customer] OFF

SET IDENTITY_INSERT [dbo].[Discount] ON 

INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (1, N'giam10', 9, CAST(10.00 AS Decimal(5, 2)), CAST(N'2026-01-01' AS Date), CAST(N'2026-12-31' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (2, N'giam15', 50, CAST(15.00 AS Decimal(5, 2)), CAST(N'2026-01-01' AS Date), CAST(N'2026-12-31' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (3, N'giam20', 30, CAST(20.00 AS Decimal(5, 2)), CAST(N'2026-01-01' AS Date), CAST(N'2026-12-31' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (4, N'giam25', 20, CAST(25.00 AS Decimal(5, 2)), CAST(N'2026-01-01' AS Date), CAST(N'2026-12-31' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (5, N'giam30', 0, CAST(30.00 AS Decimal(5, 2)), CAST(N'2026-01-01' AS Date), CAST(N'2026-12-31' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (6, N'giam5', 200, CAST(5.00 AS Decimal(5, 2)), CAST(N'2026-01-01' AS Date), CAST(N'2026-12-31' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (7, N'giam50', 4, CAST(50.00 AS Decimal(5, 2)), CAST(N'2026-01-01' AS Date), CAST(N'2026-12-31' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (8, N'GIAM95%', 94, CAST(95.00 AS Decimal(5, 2)), CAST(N'2026-11-09' AS Date), CAST(N'2026-11-17' AS Date), NULL, NULL, 1)
SET IDENTITY_INSERT [dbo].[Discount] OFF

SET IDENTITY_INSERT [dbo].[Payment] ON 

INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (1, 1, CAST(N'2025-07-09T12:00:00.0000000' AS DateTime2), CAST(500000.00 AS Decimal(18, 2)), N'cash', N'paid', N'TXN-0001', NULL, NULL, CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (2, 2, CAST(N'2025-07-10T12:00:00.0000000' AS DateTime2), CAST(1600000.00 AS Decimal(18, 2)), N'bank_transfer', N'paid', N'TXN-0002', NULL, NULL, CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (3, 3, CAST(N'2025-07-10T12:00:00.0000000' AS DateTime2), CAST(800000.00 AS Decimal(18, 2)), N'e_wallet', N'paid', N'TXN-0003', 7, NULL, CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (4, 4, CAST(N'2025-07-14T12:00:00.0000000' AS DateTime2), CAST(3600000.00 AS Decimal(18, 2)), N'credit_card', N'paid', N'TXN-0004', 1, NULL, CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (5, 6, NULL, CAST(29500.00 AS Decimal(18, 2)), N'bank_transfer', N'failed', N'HP-3ACC12B6773B4467A77B448C3F76AB15', 7, CAST(29500.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (6, 6, NULL, CAST(29500.00 AS Decimal(18, 2)), N'bank_transfer', N'failed', N'HP-A270E3DC6AF0425DBEC995E598C48310', 7, CAST(29500.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (7, 6, NULL, CAST(29500.00 AS Decimal(18, 2)), N'bank_transfer', N'failed', N'HP-C2BE49954EE54F6B9587D0431F92E7CF', 7, CAST(29500.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (8, 7, NULL, CAST(59000.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-8664595688850261', NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (9, 8, NULL, CAST(2950.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-1557107511252034', 8, CAST(56050.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (10, 9, NULL, CAST(2950.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-1122091708441414', 8, CAST(56050.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (11, 10, NULL, CAST(2950.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-4661494590856210', 8, CAST(56050.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (12, 11, CAST(N'2026-11-10T19:40:57.9625943' AS DateTime2), CAST(2950.00 AS Decimal(18, 2)), N'bank_transfer', N'refunded', N'PAYOS-3614335292702286', 8, CAST(56050.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (13, 12, NULL, CAST(11800.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-8285634446953233', 8, CAST(448400.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount]) VALUES (14, 13, NULL, CAST(2950.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-3628597971406022', 8, CAST(56050.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)))
SET IDENTITY_INSERT [dbo].[Payment] OFF

SET IDENTITY_INSERT [dbo].[Review] ON 

INSERT [dbo].[Review] ([ReviewID], [BookingID], [Comment], [Star], [ReviewDate], [ReviewStatus]) VALUES (1, 3, N'Very good experience, clean room!', CAST(5.0 AS Decimal(2, 1)), CAST(N'2025-07-10T15:00:00.0000000' AS DateTime2), N'approved')
INSERT [dbo].[Review] ([ReviewID], [BookingID], [Comment], [Star], [ReviewDate], [ReviewStatus]) VALUES (2, 4, N'Room was fine but a bit noisy.', CAST(4.5 AS Decimal(2, 1)), CAST(N'2025-07-20T15:00:00.0000000' AS DateTime2), N'approved')
SET IDENTITY_INSERT [dbo].[Review] OFF

SET IDENTITY_INSERT [dbo].[Room] ON 

INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (100, 1, N'available')
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (101, 2, N'available')
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (102, 3, N'available')
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (103, 3, N'maintenance')
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (104, 4, N'available')
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (105, 1, N'available')
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (106, 2, N'available')
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status]) VALUES (107, 3, N'available')
SET IDENTITY_INSERT [dbo].[Room] OFF

SET IDENTITY_INSERT [dbo].[RoomType] ON 

INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive]) VALUES (1, N'Standard Room', N'A comfortable and affordable room ideal for solo travelers.', CAST(59000.00 AS Decimal(18, 2)), 1, 2, N'roomType/standard-room.jpg', 1)
INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive]) VALUES (2, N'Junior Suite', N'A stylish and spacious suite ideal for couples.', CAST(89000.00 AS Decimal(18, 2)), 1, 2, N'roomType/junior-1.jpg', 1)
INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive]) VALUES (3, N'Family Suite', N'A spacious and fully equipped suite perfect for families.', CAST(129000.00 AS Decimal(18, 2)), 2, 6, N'roomType/family-suite-1.jpg', 1)
INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive]) VALUES (4, N'VIP Room', N'Exclusive VIP suite with luxurious amenities.', CAST(299000.00 AS Decimal(18, 2)), 2, 4, N'roomType/junior-3.jpg', 1)
SET IDENTITY_INSERT [dbo].[RoomType] OFF
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 1)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 2)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 3)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 1)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 2)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 3)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 5)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 1)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 2)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 3)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 4)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 5)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 1)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 2)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 3)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 4)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 5)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 6)

SET IDENTITY_INSERT [dbo].[User] ON 

INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (9, N'vu', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Vu', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (10, N'np', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Nguyen', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (11, N'khang1', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Khang', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (12, N'j97', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Phat', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (13, N'phat1111', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Phat', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (14, N'nhan', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Nhan', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (15, N'wren', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Wren', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (1005, N'admin', N'21232f297a57a5a743894a0e4a801fc3', N'admin', N'Administrator', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (1013, N'khang1111', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Khang1111', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (2013, N'khang2', N'e10adc3949ba59abbe56e057f20f883e', N'customer', N'Khang', N'active', CAST(N'2026-11-10T18:44:15.1064603' AS DateTime2))
SET IDENTITY_INSERT [dbo].[User] OFF

SET IDENTITY_INSERT [dbo].[Wallet] ON 

INSERT [dbo].[Wallet] ([WalletID], [CustomerID], [Balance]) VALUES (1, 10, CAST(2950 AS Decimal(18, 0)))
SET IDENTITY_INSERT [dbo].[Wallet] OFF

SET IDENTITY_INSERT [dbo].[WalletTransaction] ON 

INSERT [dbo].[WalletTransaction] ([WalletTransactionID], [WalletID], [BookingID], [Amount], [TransactionType], [Description], [CreatedAt]) VALUES (1, 1, 11, CAST(2950 AS Decimal(18, 0)), N'refund', N'Booking cancellation refund', CAST(N'2026-11-10T19:48:36.8682778' AS DateTime2))
SET IDENTITY_INSERT [dbo].[WalletTransaction] OFF

/****** Object:  Index [UX_AccountContact_Email]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_AccountContact_Email] ON [dbo].[AccountContact]
(
	[Email] ASC
)
WHERE ([Email] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UQ__Amenity__737584F6497878CF]    Script Date: 11/10/2026 7:55:15 PM ******/
ALTER TABLE [dbo].[Amenity] ADD UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UX_Booking_OfflineRequestKey]    Script Date: 11/10/2026 7:55:15 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_Booking_OfflineRequestKey] ON [dbo].[Booking]
(
	[OfflineRequestKey] ASC
)
WHERE ([OfflineRequestKey] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UQ__Customer__1788CCAD763E776E]    Script Date: 11/10/2026 7:55:15 PM ******/
ALTER TABLE [dbo].[Customer] ADD UNIQUE NONCLUSTERED 
(
	[UserID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UQ__Discount__A25C5AA74EE472A9]    Script Date: 11/10/2026 7:55:15 PM ******/
ALTER TABLE [dbo].[Discount] ADD UNIQUE NONCLUSTERED 
(
	[Code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UQ__Payment__D85E702659757E50]    Script Date: 11/10/2026 7:55:15 PM ******/
ALTER TABLE [dbo].[Payment] ADD UNIQUE NONCLUSTERED 
(
	[TransactionCode] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UQ_Review_Booking]    Script Date: 11/10/2026 7:55:15 PM ******/
ALTER TABLE [dbo].[Review] ADD  CONSTRAINT [UQ_Review_Booking] UNIQUE NONCLUSTERED 
(
	[BookingID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UQ__User__536C85E4A7AC9EB3]    Script Date: 11/10/2026 7:55:15 PM ******/
ALTER TABLE [dbo].[User] ADD UNIQUE NONCLUSTERED 
(
	[Username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]

/****** Object:  Index [UQ__Wallet__A4AE64B902C5880E]    Script Date: 11/10/2026 7:55:15 PM ******/
ALTER TABLE [dbo].[Wallet] ADD UNIQUE NONCLUSTERED 
(
	[CustomerID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_BookingDate]  DEFAULT (sysdatetime()) FOR [BookingDate]
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_Status]  DEFAULT ('pending') FOR [Status]
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_TotalPrice]  DEFAULT ((0)) FOR [TotalPrice]
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_OnlineDepositPolicy]  DEFAULT ((0)) FOR [OnlineDepositPolicy]
ALTER TABLE [dbo].[BookingDetail] ADD  CONSTRAINT [DF_BookingDetail_GuestCount]  DEFAULT ((1)) FOR [GuestCount]
ALTER TABLE [dbo].[Discount] ADD  CONSTRAINT [DF_Discount_Quantity]  DEFAULT ((0)) FOR [Quantity]
ALTER TABLE [dbo].[Discount] ADD  CONSTRAINT [DF_Discount_IsActive]  DEFAULT ((1)) FOR [IsActive]
ALTER TABLE [dbo].[Payment] ADD  CONSTRAINT [DF_Payment_Status]  DEFAULT ('pending') FOR [PaymentStatus]
ALTER TABLE [dbo].[Payment] ADD  CONSTRAINT [DF_Payment_RefundedAmount]  DEFAULT ((0)) FOR [RefundedAmount]
ALTER TABLE [dbo].[Review] ADD  CONSTRAINT [DF_Review_ReviewDate]  DEFAULT (sysdatetime()) FOR [ReviewDate]
ALTER TABLE [dbo].[Review] ADD  CONSTRAINT [DF_Review_Status]  DEFAULT ('pending') FOR [ReviewStatus]
ALTER TABLE [dbo].[Room] ADD  CONSTRAINT [DF_Room_Status]  DEFAULT ('available') FOR [Status]
ALTER TABLE [dbo].[RoomType] ADD  CONSTRAINT [DF_RoomType_IsActive]  DEFAULT ((1)) FOR [IsActive]
ALTER TABLE [dbo].[User] ADD  CONSTRAINT [DF_User_AccountStatus]  DEFAULT ('active') FOR [AccountStatus]
ALTER TABLE [dbo].[User] ADD  CONSTRAINT [DF_User_CreatedAt]  DEFAULT (sysdatetime()) FOR [CreatedAt]
ALTER TABLE [dbo].[Wallet] ADD  DEFAULT ((0)) FOR [Balance]
ALTER TABLE [dbo].[WalletTransaction] ADD  DEFAULT (sysdatetime()) FOR [CreatedAt]
ALTER TABLE [dbo].[AccountContact]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[User] ([UserID])
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [FK_Booking_CreatedByUser] FOREIGN KEY([CreatedByUserID])
REFERENCES [dbo].[User] ([UserID])
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [FK_Booking_CreatedByUser]
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [FK_Booking_Customer] FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customer] ([CustomerID])
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [FK_Booking_Customer]
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [FK_BookingDetail_Booking] FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [FK_BookingDetail_Booking]
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [FK_BookingDetail_Room] FOREIGN KEY([RoomNumber])
REFERENCES [dbo].[Room] ([RoomNumber])
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [FK_BookingDetail_Room]
ALTER TABLE [dbo].[Customer]  WITH CHECK ADD  CONSTRAINT [FK_Customer_User] FOREIGN KEY([UserID])
REFERENCES [dbo].[User] ([UserID])
ALTER TABLE [dbo].[Customer] CHECK CONSTRAINT [FK_Customer_User]
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [FK_Payment_Booking] FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [FK_Payment_Booking]
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [FK_Payment_Discount] FOREIGN KEY([DiscountID])
REFERENCES [dbo].[Discount] ([DiscountID])
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [FK_Payment_Discount]
ALTER TABLE [dbo].[Review]  WITH CHECK ADD  CONSTRAINT [FK_Review_Booking] FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
ALTER TABLE [dbo].[Review] CHECK CONSTRAINT [FK_Review_Booking]
ALTER TABLE [dbo].[Room]  WITH CHECK ADD  CONSTRAINT [FK_Room_RoomType] FOREIGN KEY([RoomTypeID])
REFERENCES [dbo].[RoomType] ([RoomTypeID])
ALTER TABLE [dbo].[Room] CHECK CONSTRAINT [FK_Room_RoomType]
ALTER TABLE [dbo].[RoomTypeAmenity]  WITH CHECK ADD  CONSTRAINT [FK_RoomTypeAmenity_Amenity] FOREIGN KEY([AmenityID])
REFERENCES [dbo].[Amenity] ([AmenityID])
ALTER TABLE [dbo].[RoomTypeAmenity] CHECK CONSTRAINT [FK_RoomTypeAmenity_Amenity]
ALTER TABLE [dbo].[RoomTypeAmenity]  WITH CHECK ADD  CONSTRAINT [FK_RoomTypeAmenity_RoomType] FOREIGN KEY([RoomTypeID])
REFERENCES [dbo].[RoomType] ([RoomTypeID])
ALTER TABLE [dbo].[RoomTypeAmenity] CHECK CONSTRAINT [FK_RoomTypeAmenity_RoomType]
ALTER TABLE [dbo].[Wallet]  WITH CHECK ADD FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customer] ([CustomerID])
ALTER TABLE [dbo].[WalletTransaction]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
ALTER TABLE [dbo].[WalletTransaction]  WITH CHECK ADD FOREIGN KEY([WalletID])
REFERENCES [dbo].[Wallet] ([WalletID])
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [CK_Booking_Status] CHECK  (([Status]='completed' OR [Status]='checked_out' OR [Status]='checked_in' OR [Status]='cancelled' OR [Status]='confirmed' OR [Status]='pending'))
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [CK_Booking_Status]
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [CK_Booking_TotalPrice] CHECK  (([TotalPrice]>=(0)))
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [CK_Booking_TotalPrice]
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_Date] CHECK  (([CheckOutDate]>[CheckInDate]))
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_Date]
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_GuestCount] CHECK  (([GuestCount]>(0)))
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_GuestCount]
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_Price] CHECK  (([PricePerNight]>=(0)))
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_Price]
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_Date] CHECK  (([EndDate] IS NULL OR [StartDate] IS NULL OR [EndDate]>=[StartDate]))
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_Date]
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_MaximumDiscount] CHECK  (([MaximumDiscount] IS NULL OR [MaximumDiscount]>=(0)))
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_MaximumDiscount]
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_MinimumAmount] CHECK  (([MinimumAmount] IS NULL OR [MinimumAmount]>=(0)))
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_MinimumAmount]
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_Quantity] CHECK  (([Quantity]>=(0)))
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_Quantity]
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_SaleOff] CHECK  (([SaleOff]>=(0) AND [SaleOff]<=(100)))
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_SaleOff]
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [CK_Payment_Amount] CHECK  (([Amount]>=(0)))
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [CK_Payment_Amount]
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [CK_Payment_Method] CHECK  (([PaymentMethod]='e_wallet' OR [PaymentMethod]='credit_card' OR [PaymentMethod]='bank_transfer' OR [PaymentMethod]='cash'))
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [CK_Payment_Method]
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [CK_Payment_Status] CHECK  (([PaymentStatus]='refunded' OR [PaymentStatus]='failed' OR [PaymentStatus]='paid' OR [PaymentStatus]='pending'))
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [CK_Payment_Status]
ALTER TABLE [dbo].[Review]  WITH CHECK ADD  CONSTRAINT [CK_Review_Star] CHECK  (([Star]>=(1) AND [Star]<=(5)))
ALTER TABLE [dbo].[Review] CHECK CONSTRAINT [CK_Review_Star]
ALTER TABLE [dbo].[Review]  WITH CHECK ADD  CONSTRAINT [CK_Review_Status] CHECK  (([ReviewStatus]='rejected' OR [ReviewStatus]='approved' OR [ReviewStatus]='pending'))
ALTER TABLE [dbo].[Review] CHECK CONSTRAINT [CK_Review_Status]
ALTER TABLE [dbo].[Room]  WITH CHECK ADD  CONSTRAINT [CK_Room_Status] CHECK  (([Status]='out_of_service' OR [Status]='maintenance' OR [Status]='available'))
ALTER TABLE [dbo].[Room] CHECK CONSTRAINT [CK_Room_Status]
ALTER TABLE [dbo].[RoomType]  WITH CHECK ADD  CONSTRAINT [CK_RoomType_Beds] CHECK  (([Beds]>(0)))
ALTER TABLE [dbo].[RoomType] CHECK CONSTRAINT [CK_RoomType_Beds]
ALTER TABLE [dbo].[RoomType]  WITH CHECK ADD  CONSTRAINT [CK_RoomType_Capacity] CHECK  (([Capacity]>(0)))
ALTER TABLE [dbo].[RoomType] CHECK CONSTRAINT [CK_RoomType_Capacity]
ALTER TABLE [dbo].[RoomType]  WITH CHECK ADD  CONSTRAINT [CK_RoomType_Price] CHECK  (([PricePerNight]>=(0)))
ALTER TABLE [dbo].[RoomType] CHECK CONSTRAINT [CK_RoomType_Price]
ALTER TABLE [dbo].[User]  WITH CHECK ADD  CONSTRAINT [CK_User_AccountStatus] CHECK  (([AccountStatus]='inactive' OR [AccountStatus]='locked' OR [AccountStatus]='active'))
ALTER TABLE [dbo].[User] CHECK CONSTRAINT [CK_User_AccountStatus]
ALTER TABLE [dbo].[User]  WITH CHECK ADD  CONSTRAINT [CK_User_Role] CHECK  (([Role]='it_support' OR [Role]='manager' OR [Role]='admin' OR [Role]='staff' OR [Role]='customer'))
ALTER TABLE [dbo].[User] CHECK CONSTRAINT [CK_User_Role]

EXEC sys.sp_executesql N'
/* =========================================================
   2. PROCEDURE HỦY BOOKING OFFLINE HẾT HẠN

   Chỉ hủy khi:
   - Là booking offline.
   - Đang pending.
   - Đã hết hạn giữ phòng.
   - Không có giao dịch thanh toán paid.

   Không hủy booking đã confirmed.
   ========================================================= */

CREATE   PROCEDURE [dbo].[ExpireOfflineBookingHolds]
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        ;WITH due AS (
            SELECT TOP (100) b.*
            FROM dbo.Booking b
                WITH (UPDLOCK, READPAST, ROWLOCK)
            WHERE b.CreatedByUserID IS NOT NULL
              AND b.Status = ''pending''
              AND b.HoldExpiresAt <= SYSUTCDATETIME()
            ORDER BY
                b.HoldExpiresAt,
                b.BookingID
        )
        UPDATE due
        SET Status = ''cancelled'',
            CancellationDate = SYSDATETIME(),
            CancellationReason = N''Hết hạn thanh toán''
        WHERE NOT EXISTS (
            SELECT 1
            FROM dbo.Payment p WITH (HOLDLOCK)
            WHERE p.BookingID = due.BookingID
              AND p.PaymentStatus = ''paid''
        );

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH;
END;
';

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
