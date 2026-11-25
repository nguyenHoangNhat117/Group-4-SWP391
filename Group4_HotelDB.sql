USE [master]
GO
/****** Object:  Database [HotelDB]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE DATABASE [HotelDB]
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [HotelDB].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [HotelDB] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [HotelDB] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [HotelDB] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [HotelDB] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [HotelDB] SET ARITHABORT OFF 
GO
ALTER DATABASE [HotelDB] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [HotelDB] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [HotelDB] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [HotelDB] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [HotelDB] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [HotelDB] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [HotelDB] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [HotelDB] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [HotelDB] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [HotelDB] SET  ENABLE_BROKER 
GO
ALTER DATABASE [HotelDB] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [HotelDB] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [HotelDB] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [HotelDB] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [HotelDB] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [HotelDB] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [HotelDB] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [HotelDB] SET RECOVERY FULL 
GO
ALTER DATABASE [HotelDB] SET  MULTI_USER 
GO
ALTER DATABASE [HotelDB] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [HotelDB] SET DB_CHAINING OFF 
GO
ALTER DATABASE [HotelDB] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [HotelDB] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [HotelDB] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [HotelDB] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
ALTER DATABASE [HotelDB] SET QUERY_STORE = ON
GO
ALTER DATABASE [HotelDB] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [HotelDB]
GO
/****** Object:  Table [dbo].[AccountContact]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Amenity]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Amenity](
	[AmenityID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](100) NOT NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[AmenityID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Booking]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
	[ActualCheckInAt] [datetime2](7) NULL,
	[ActualCheckOutAt] [datetime2](7) NULL,
	[IdentityVerifiedBy] [int] NULL,
	[ActualGuestCount] [int] NULL,
	[OnlineRequestKey] [varchar](36) NULL,
	[Adults] [int] NULL,
	[Children] [int] NULL,
	[OnlinePaymentPercent] [tinyint] NULL,
PRIMARY KEY CLUSTERED 
(
	[BookingID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[BookingDetail]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[BookingDetail](
	[BookingDetailID] [int] IDENTITY(1,1) NOT NULL,
	[BookingID] [int] NOT NULL,
	[RoomNumber] [int] NULL,
	[CheckInDate] [date] NOT NULL,
	[CheckOutDate] [date] NOT NULL,
	[PricePerNight] [decimal](18, 2) NOT NULL,
	[GuestCount] [int] NOT NULL,
	[SpecialRequest] [nvarchar](500) NULL,
	[RoomTypeID] [int] NULL,
	[GuestName] [nvarchar](120) NULL,
	[BookedAdults] [int] NULL,
	[BookedChildren] [int] NULL,
	[IsProvisionalRoom] [bit] NOT NULL,
	[ActualGuestCount] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[BookingDetailID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CounterQrCheckout]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CounterQrCheckout](
	[OrderCode] [bigint] NOT NULL,
	[BookingID] [int] NOT NULL,
	[PaymentID] [int] NOT NULL,
	[StaffID] [int] NOT NULL,
	[RequestKey] [uniqueidentifier] NOT NULL,
	[OriginalTotal] [decimal](18, 2) NOT NULL,
	[Payable] [decimal](18, 2) NOT NULL,
	[PreviouslyPaid] [decimal](18, 2) NOT NULL,
	[ActualReceived] [bigint] NULL,
	[ExpiresEpoch] [bigint] NOT NULL,
	[Active] [bit] NOT NULL,
	[State] [varchar](20) NOT NULL,
	[CheckoutUrl] [nvarchar](500) NULL,
	[LinkAttempted] [bit] NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[CheckedAt] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[OrderCode] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Customer]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Customer](
	[CustomerID] [int] IDENTITY(1,1) NOT NULL,
	[FirstName] [nvarchar](50) NULL,
	[LastName] [nvarchar](50) NULL,
	[Email] [nvarchar](100) NULL,
	[Phone] [nvarchar](20) NULL,
	[UserID] [int] NULL,
	[Country] [nvarchar](50) NULL,
	[Street] [nvarchar](100) NULL,
	[City] [nvarchar](50) NULL,
	[Avatar] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[CustomerID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Discount]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[OfflineBookingEvent]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[OfflineBookingEvent](
	[EventID] [bigint] IDENTITY(1,1) NOT NULL,
	[BookingID] [int] NOT NULL,
	[ActorUserID] [int] NOT NULL,
	[RequestKey] [uniqueidentifier] NOT NULL,
	[Action] [nvarchar](30) NOT NULL,
	[Note] [nvarchar](500) NULL,
	[Amount] [decimal](18, 2) NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[EventID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[OfflineCharge]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[OfflineCharge](
	[ChargeID] [int] IDENTITY(1,1) NOT NULL,
	[BookingID] [int] NOT NULL,
	[Description] [nvarchar](500) NOT NULL,
	[Amount] [decimal](18, 2) NOT NULL,
	[CreatedByUserID] [int] NOT NULL,
	[RequestKey] [uniqueidentifier] NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ChargeID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[OfflineRefund]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[OfflineRefund](
	[RefundID] [int] IDENTITY(1,1) NOT NULL,
	[BookingID] [int] NOT NULL,
	[Amount] [decimal](18, 2) NOT NULL,
	[Fee] [decimal](18, 2) NOT NULL,
	[Status] [nvarchar](20) NOT NULL,
	[Method] [nvarchar](30) NULL,
	[Reference] [nvarchar](100) NULL,
	[PaidByUserID] [int] NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[CompletedAt] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[RefundID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Payment]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
	[CollectedByUserID] [int] NULL,
	[CounterRequestKey] [uniqueidentifier] NULL,
	[CounterReference] [nvarchar](100) NULL,
PRIMARY KEY CLUSTERED 
(
	[PaymentID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Review]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Room]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Room](
	[RoomNumber] [int] IDENTITY(100,1) NOT NULL,
	[RoomTypeID] [int] NOT NULL,
	[Status] [nvarchar](20) NOT NULL,
	[NeedsCleaning] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[RoomNumber] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomInventorySettings]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoomInventorySettings](
	[SettingID] [tinyint] NOT NULL,
	[MaxRooms] [int] NOT NULL,
 CONSTRAINT [PK_RoomInventorySettings] PRIMARY KEY CLUSTERED 
(
	[SettingID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomServiceEvent]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoomServiceEvent](
	[EventID] [bigint] IDENTITY(1,1) NOT NULL,
	[RoomNumber] [int] NOT NULL,
	[ActorUserID] [int] NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[EventID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomType]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoomType](
	[RoomTypeID] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](50) NOT NULL,
	[Description] [nvarchar](500) NULL,
	[PricePerNight] [decimal](18, 2) NOT NULL,
	[Beds] [int] NOT NULL,
	[Capacity] [int] NOT NULL,
	[Picture] [nvarchar](255) NULL,
	[IsActive] [bit] NOT NULL,
	[MaxChildren] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[RoomTypeID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoomTypeAmenity]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoomTypeAmenity](
	[RoomTypeID] [int] NOT NULL,
	[AmenityID] [int] NOT NULL,
 CONSTRAINT [PK_RoomTypeAmenity] PRIMARY KEY CLUSTERED 
(
	[RoomTypeID] ASC,
	[AmenityID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[User]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Wallet]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Wallet](
	[WalletID] [int] IDENTITY(1,1) NOT NULL,
	[CustomerID] [int] NOT NULL,
	[Balance] [decimal](18, 0) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[WalletID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[WalletTransaction]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[WithdrawalRequest]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[WithdrawalRequest](
	[WithdrawalID] [int] IDENTITY(1,1) NOT NULL,
	[CustomerID] [int] NOT NULL,
	[Amount] [decimal](18, 2) NOT NULL,
	[BankName] [nvarchar](100) NOT NULL,
	[AccountNumber] [nvarchar](100) NOT NULL,
	[AccountHolder] [nvarchar](150) NOT NULL,
	[Status] [varchar](20) NOT NULL,
	[RejectReason] [nvarchar](500) NULL,
	[ReviewedBy] [int] NULL,
	[ReviewedAt] [datetime2](7) NULL,
	[PaidBy] [int] NULL,
	[PaidAt] [datetime2](7) NULL,
	[PaymentReference] [nvarchar](100) NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[WithdrawalID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (1, N'Trị', N'Quản', N'admin@example.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (2, N'Hàng', N'Khách', N'customer@example.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (3, N'Lý', N'Quản', N'manager@example.com', NULL, NULL, NULL, NULL)
INSERT [dbo].[AccountContact] ([UserID], [FirstName], [LastName], [Email], [Phone], [Country], [City], [Street]) VALUES (4, N'Viên', N'Nhân', N'staff@example.com', NULL, NULL, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[Amenity] ON 

INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (1, N'2 Smart TV 65 inch (Phòng khách & Phòng ngủ)', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (2, N'Áo choàng tắm cao cấp', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (3, N'Bàn ăn gia đình 4 ghế', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (4, N'Bàn làm việc giám đốc', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (5, N'Bộ Sofa tiếp khách', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (6, N'Bồn tắm nằm', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (7, N'Bồn tắm ngâm có view nhìn ra ngoài trời', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (8, N'Bồn tắm sục (Jacuzzi)', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (9, N'Dịch vụ Đưa đón sân bay bằng xe siêu sang (Limousine/Maybach)', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (10, N'Điều hòa trung tâm', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (11, N'Két sắt điện tử', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (12, N'Khu vực Bếp riêng', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (13, N'Lối đi/Thang máy riêng biệt', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (14, N'Máy pha cà phê', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (15, N'Máy pha trà/cà phê cơ bản', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (16, N'Minibar', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (17, N'Phòng tắm đứng', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (18, N'Phòng xông hơi (Sauna) cá nhân', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (19, N'Quầy Bar riêng với các loại rượu mạnh cao cấp', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (20, N'Smart TV 55 inch', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (21, N'Tủ lạnh size lớn', 1)
INSERT [dbo].[Amenity] ([AmenityID], [Name], [IsActive]) VALUES (22, N'Wifi tốc độ cao', 1)
SET IDENTITY_INSERT [dbo].[Amenity] OFF
GO
SET IDENTITY_INSERT [dbo].[Booking] ON 

INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (1, 1, CAST(N'2026-11-22T22:08:28.6376369' AS DateTime2), N'cancelled', CAST(60000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-22T22:10:31.3166576' AS DateTime2), N'Cancelled by customer', NULL, NULL, NULL, CAST(N'2026-11-22T15:23:28.6376369' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'312b25c1-607d-42d0-8e70-89d9fc25200a', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (2, 1, CAST(N'2026-11-22T22:10:48.6079768' AS DateTime2), N'cancelled', CAST(60000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-22T22:45:37.9242043' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, CAST(N'2026-11-22T15:25:48.6079768' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'eadf902d-3cfe-4c69-8576-9743b7d5a336', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (3, 1, CAST(N'2026-11-22T23:28:56.8702095' AS DateTime2), N'cancelled', CAST(60000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-22T23:29:11.6461863' AS DateTime2), N'Cancelled by customer', NULL, NULL, NULL, CAST(N'2026-11-22T16:43:56.8702095' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'56c8ce5d-6919-4b11-8332-d582a1080949', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (4, 1, CAST(N'2026-11-22T23:30:30.3976262' AS DateTime2), N'cancelled', CAST(60000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-22T23:30:49.6761537' AS DateTime2), N'Cancelled by customer', NULL, NULL, NULL, CAST(N'2026-11-22T16:45:30.3976262' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'de0dc6b6-458b-4a9b-90bf-a3c7e5c9ea67', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (5, 1, CAST(N'2026-11-22T23:31:05.7763978' AS DateTime2), N'cancelled', CAST(60000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-23T01:09:29.1923516' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, CAST(N'2026-11-22T16:46:05.7763978' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'd1572e73-8e0f-40e7-bdc3-d586df889364', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (6, 1, CAST(N'2026-11-23T01:15:06.5402581' AS DateTime2), N'confirmed', CAST(6000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-11-22T18:30:06.5402581' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'4ce920e4-2aae-44f7-aa41-7ecdc47b924f', 1, 1, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (7, 1, CAST(N'2026-11-23T01:49:01.7539889' AS DateTime2), N'cancelled', CAST(60000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-23T02:07:27.1435519' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, CAST(N'2026-11-22T19:04:01.7539889' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'af285630-1733-4ff6-8ab2-7695af48ca01', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (8, 1, CAST(N'2026-11-23T01:59:32.8902669' AS DateTime2), N'cancelled', CAST(120000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-23T02:17:44.9742342' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, CAST(N'2026-11-22T19:14:32.8902669' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', N'Vietnam', 1, NULL, NULL, NULL, NULL, N'b2b6bd6f-2ae9-4411-8dd9-56563a972bfe', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (9, 9, CAST(N'2026-11-23T02:03:23.9346604' AS DateTime2), N'confirmed', CAST(6000.00 AS Decimal(18, 2)), N'', NULL, NULL, 4, N'e974f456-032a-418d-a193-c72723367608', N'G95', CAST(N'2026-11-22T19:33:23.9346604' AS DateTime2), 50, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (10, 1, CAST(N'2026-11-23T11:20:02.3036108' AS DateTime2), N'confirmed', CAST(6000.00 AS Decimal(18, 2)), NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-11-23T04:35:02.3036108' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', NULL, 1, NULL, NULL, NULL, NULL, N'102c0c16-b282-422f-8ccb-b283e592b6d1', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (11, 1, CAST(N'2026-11-23T11:33:29.2577778' AS DateTime2), N'cancelled', CAST(12000000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-23T11:54:02.6590523' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, CAST(N'2026-11-23T04:48:29.2577778' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', NULL, 1, NULL, NULL, NULL, NULL, N'bb845ba2-8b0c-4820-8a57-09b783aab2bf', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (12, 1, CAST(N'2026-11-23T11:33:53.9563389' AS DateTime2), N'cancelled', CAST(12000000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-23T11:54:02.6590523' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, CAST(N'2026-11-23T04:48:53.9563389' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', NULL, 1, NULL, NULL, NULL, NULL, N'c7a61d1b-06fc-46c6-b9d4-f18716c9e73a', 1, 0, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (13, 10, CAST(N'2026-11-20T00:00:00.0000000' AS DateTime2), N'completed', CAST(60000.00 AS Decimal(18, 2)), N'[DEMO] Review loại phòng 1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (14, 10, CAST(N'2026-11-20T00:00:00.0000000' AS DateTime2), N'completed', CAST(120000.00 AS Decimal(18, 2)), N'[DEMO] Review loại phòng 2', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (15, 10, CAST(N'2026-11-20T00:00:00.0000000' AS DateTime2), N'completed', CAST(180000.00 AS Decimal(18, 2)), N'[DEMO] Review loại phòng 3', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (16, 10, CAST(N'2026-11-20T00:00:00.0000000' AS DateTime2), N'completed', CAST(12000000.00 AS Decimal(18, 2)), N'[DEMO] Review loại phòng 4', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (17, 10, CAST(N'2026-11-20T00:00:00.0000000' AS DateTime2), N'completed', CAST(999999.00 AS Decimal(18, 2)), N'[DEMO] Review loại phòng 5', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (18, 1, CAST(N'2026-11-23T12:46:09.8251828' AS DateTime2), N'cancelled', CAST(120000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-23T14:50:04.9088955' AS DateTime2), N'Expired pending booking', NULL, NULL, NULL, CAST(N'2026-11-23T06:01:09.8251828' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', NULL, 1, NULL, NULL, NULL, NULL, N'305350f5-e73c-4a76-8228-18ce3fc0e73a', 3, 1, 50)
INSERT [dbo].[Booking] ([BookingID], [CustomerID], [BookingDate], [Status], [TotalPrice], [SpecialRequest], [CancellationDate], [CancellationReason], [CreatedByUserID], [OfflineRequestKey], [OfflinePromotionCode], [HoldExpiresAt], [OfflineDepositPercent], [ContactName], [ContactPhone], [ContactEmail], [ContactCountry], [OnlineDepositPolicy], [ActualCheckInAt], [ActualCheckOutAt], [IdentityVerifiedBy], [ActualGuestCount], [OnlineRequestKey], [Adults], [Children], [OnlinePaymentPercent]) VALUES (19, 1, CAST(N'2026-11-23T17:29:30.3617357' AS DateTime2), N'cancelled', CAST(6000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-11-23T17:30:21.5113302' AS DateTime2), N'Cancelled by customer', NULL, NULL, NULL, CAST(N'2026-11-23T10:44:30.3617357' AS DateTime2), NULL, N'Khách Hàng', N'+84944452580', N'customer@example.com', NULL, 1, NULL, NULL, NULL, NULL, N'e0b306be-d16c-4f62-8bad-782db0b7a57f', 1, 0, 50)
SET IDENTITY_INSERT [dbo].[Booking] OFF
GO
SET IDENTITY_INSERT [dbo].[BookingDetail] ON 

INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (1, 1, NULL, CAST(N'2026-11-22' AS Date), CAST(N'2026-11-23' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (2, 2, NULL, CAST(N'2026-11-22' AS Date), CAST(N'2026-11-23' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (3, 3, NULL, CAST(N'2026-11-22' AS Date), CAST(N'2026-11-23' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (4, 4, NULL, CAST(N'2026-11-22' AS Date), CAST(N'2026-11-23' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (5, 5, NULL, CAST(N'2026-11-22' AS Date), CAST(N'2026-11-23' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (6, 6, NULL, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (7, 7, NULL, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (8, 8, NULL, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(120000.00 AS Decimal(18, 2)), 1, NULL, 2, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (9, 9, NULL, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(120000.00 AS Decimal(18, 2)), 1, N'', 2, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (10, 10, 110, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, 1, 0, 1, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (11, 11, 106, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(12000000.00 AS Decimal(18, 2)), 1, NULL, 4, NULL, 1, 0, 1, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (12, 12, 106, CAST(N'2026-11-25' AS Date), CAST(N'2026-11-26' AS Date), CAST(12000000.00 AS Decimal(18, 2)), 1, NULL, 4, NULL, 1, 0, 1, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (13, 13, 101, CAST(N'2026-11-21' AS Date), CAST(N'2026-11-22' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (14, 14, 103, CAST(N'2026-11-21' AS Date), CAST(N'2026-11-22' AS Date), CAST(120000.00 AS Decimal(18, 2)), 1, NULL, 2, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (15, 15, 104, CAST(N'2026-11-21' AS Date), CAST(N'2026-11-22' AS Date), CAST(180000.00 AS Decimal(18, 2)), 1, NULL, 3, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (16, 16, 106, CAST(N'2026-11-21' AS Date), CAST(N'2026-11-22' AS Date), CAST(12000000.00 AS Decimal(18, 2)), 1, NULL, 4, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (17, 17, 108, CAST(N'2026-11-21' AS Date), CAST(N'2026-11-22' AS Date), CAST(999999.00 AS Decimal(18, 2)), 1, NULL, 5, NULL, NULL, NULL, 0, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (18, 18, 102, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, 1, 0, 1, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (19, 18, 114, CAST(N'2026-11-23' AS Date), CAST(N'2026-11-24' AS Date), CAST(60000.00 AS Decimal(18, 2)), 3, NULL, 1, NULL, 2, 1, 1, NULL)
INSERT [dbo].[BookingDetail] ([BookingDetailID], [BookingID], [RoomNumber], [CheckInDate], [CheckOutDate], [PricePerNight], [GuestCount], [SpecialRequest], [RoomTypeID], [GuestName], [BookedAdults], [BookedChildren], [IsProvisionalRoom], [ActualGuestCount]) VALUES (20, 19, 101, CAST(N'2026-12-07' AS Date), CAST(N'2026-12-08' AS Date), CAST(60000.00 AS Decimal(18, 2)), 1, NULL, 1, NULL, 1, 0, 1, NULL)
SET IDENTITY_INSERT [dbo].[BookingDetail] OFF
GO
INSERT [dbo].[CounterQrCheckout] ([OrderCode], [BookingID], [PaymentID], [StaffID], [RequestKey], [OriginalTotal], [Payable], [PreviouslyPaid], [ActualReceived], [ExpiresEpoch], [Active], [State], [CheckoutUrl], [LinkAttempted], [CreatedAt], [CheckedAt]) VALUES (900487596062914, 9, 9, 4, N'369a77da-7a80-40ec-98bc-afb35c9821ac', CAST(120000.00 AS Decimal(18, 2)), CAST(6000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), 3000, 1795374875, 0, N'paid', N'https://pay.payos.vn/web/24e2b8a55e694f80bff91cc4d23fb57b', 1, CAST(N'2026-11-22T19:04:35.4309386' AS DateTime2), CAST(N'2026-11-22T19:05:05.1775170' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[Customer] ON 

INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (1, N'Hàng', N'Khách', N'customer@example.com', NULL, 2, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (8, N'Kiểm thử', N'Giao diện', N'cart-ui@example.invalid', N'0901234567', 11, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (9, N'Luong', N'Dat', NULL, N'+84944452580', NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[Customer] ([CustomerID], [FirstName], [LastName], [Email], [Phone], [UserID], [Country], [Street], [City], [Avatar]) VALUES (10, N'Mẫu', N'Khách', N'demo-room-type-reviews@example.invalid', NULL, 12, NULL, NULL, NULL, NULL)
SET IDENTITY_INSERT [dbo].[Customer] OFF
GO
SET IDENTITY_INSERT [dbo].[Discount] ON 

INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (1, N'G90', 97, CAST(90.00 AS Decimal(5, 2)), CAST(N'2026-11-02' AS Date), CAST(N'2026-11-30' AS Date), NULL, NULL, 1)
INSERT [dbo].[Discount] ([DiscountID], [Code], [Quantity], [SaleOff], [StartDate], [EndDate], [MinimumAmount], [MaximumDiscount], [IsActive]) VALUES (2, N'G95', 99, CAST(95.00 AS Decimal(5, 2)), CAST(N'2026-11-02' AS Date), CAST(N'2026-11-30' AS Date), NULL, NULL, 1)
SET IDENTITY_INSERT [dbo].[Discount] OFF
GO
SET IDENTITY_INSERT [dbo].[OfflineBookingEvent] ON 

INSERT [dbo].[OfflineBookingEvent] ([EventID], [BookingID], [ActorUserID], [RequestKey], [Action], [Note], [Amount], [CreatedAt]) VALUES (1, 9, 4, N'e974f456-032a-418d-a193-c72723367608', N'create', N'Tạo đơn giữ phòng tại quầy', CAST(120000.00 AS Decimal(18, 2)), CAST(N'2026-11-22T19:03:23.9396593' AS DateTime2))
INSERT [dbo].[OfflineBookingEvent] ([EventID], [BookingID], [ActorUserID], [RequestKey], [Action], [Note], [Amount], [CreatedAt]) VALUES (2, 9, 4, N'369a77da-7a80-40ec-98bc-afb35c9821ac', N'payment', N'QR payOS / 900487596062914', CAST(3000.00 AS Decimal(18, 2)), CAST(N'2026-11-22T19:05:05.1825231' AS DateTime2))
SET IDENTITY_INSERT [dbo].[OfflineBookingEvent] OFF
GO
SET IDENTITY_INSERT [dbo].[Payment] ON 

INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (1, 1, NULL, CAST(30000.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-2352922498147322', NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (2, 2, NULL, CAST(30000.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-8536734281208668', NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (3, 3, NULL, CAST(3000.00 AS Decimal(18, 2)), N'bank_transfer', N'failed', N'PAYOS-3482113140427627', 1, CAST(54000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (4, 4, NULL, CAST(1500.00 AS Decimal(18, 2)), N'bank_transfer', N'failed', N'PAYOS-6599019873899599', 2, CAST(57000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (5, 5, NULL, CAST(30000.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-1903788717960486', NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (6, 6, CAST(N'2026-11-23T01:17:14.0601400' AS DateTime2), CAST(3000.00 AS Decimal(18, 2)), N'bank_transfer', N'paid', N'PAYOS-2800738874806669', 1, CAST(54000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (7, 7, NULL, CAST(3000.00 AS Decimal(18, 2)), N'bank_transfer', N'failed', N'PAYOS-1494018964306451', 1, CAST(54000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (8, 8, NULL, CAST(3000.00 AS Decimal(18, 2)), N'bank_transfer', N'failed', N'PAYOS-2643767266003897', 2, CAST(114000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (9, 9, CAST(N'2026-11-23T02:05:05.1799571' AS DateTime2), CAST(3000.00 AS Decimal(18, 2)), N'bank_transfer', N'paid', N'CQR-900487596062914', 2, CAST(114000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), 4, N'369a77da-7a80-40ec-98bc-afb35c9821ac', N'payOS:900487596062914')
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (10, 10, CAST(N'2026-11-23T11:20:33.3028853' AS DateTime2), CAST(3000.00 AS Decimal(18, 2)), N'bank_transfer', N'paid', N'PAYOS-3700127323553716', 1, CAST(54000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (11, 11, NULL, CAST(6000000.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-3057881327871996', NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (12, 12, NULL, CAST(6000000.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-3903576621810887', NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (13, 18, NULL, CAST(60000.00 AS Decimal(18, 2)), N'bank_transfer', N'pending', N'PAYOS-6785359009741154', NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
INSERT [dbo].[Payment] ([PaymentID], [BookingID], [PaymentDate], [Amount], [PaymentMethod], [PaymentStatus], [TransactionCode], [DiscountID], [DiscountAmount], [RefundedAmount], [CollectedByUserID], [CounterRequestKey], [CounterReference]) VALUES (14, 19, CAST(N'2026-11-23T17:30:01.5460213' AS DateTime2), CAST(3000.00 AS Decimal(18, 2)), N'bank_transfer', N'refunded', N'PAYOS-3298511976923589', 1, CAST(54000.00 AS Decimal(18, 2)), CAST(0 AS Decimal(18, 0)), NULL, NULL, NULL)
SET IDENTITY_INSERT [dbo].[Payment] OFF
GO
SET IDENTITY_INSERT [dbo].[Review] ON 

INSERT [dbo].[Review] ([ReviewID], [BookingID], [Comment], [Star], [ReviewDate], [ReviewStatus]) VALUES (1, 13, N'Giường êm, phòng sạch và cửa sổ nhìn ra thành phố rất đẹp.', CAST(4.5 AS Decimal(2, 1)), CAST(N'2026-11-23T11:51:06.0022714' AS DateTime2), N'approved')
INSERT [dbo].[Review] ([ReviewID], [BookingID], [Comment], [Star], [ReviewDate], [ReviewStatus]) VALUES (2, 14, N'Ban công nhìn ra biển rất thoáng. Buổi sáng ngồi uống cà phê ở đây rất dễ chịu.', CAST(5.0 AS Decimal(2, 1)), CAST(N'2026-11-23T11:51:06.0022714' AS DateTime2), N'approved')
INSERT [dbo].[Review] ([ReviewID], [BookingID], [Comment], [Star], [ReviewDate], [ReviewStatus]) VALUES (3, 15, N'Bàn làm việc rộng, ánh sáng tốt và phòng khá yên tĩnh.', CAST(4.5 AS Decimal(2, 1)), CAST(N'2026-11-23T11:51:06.0022714' AS DateTime2), N'approved')
INSERT [dbo].[Review] ([ReviewID], [BookingID], [Comment], [Star], [ReviewDate], [ReviewStatus]) VALUES (4, 16, N'Phòng Grand Family Suite sạch sẽ, giường dễ ngủ và nhận phòng nhanh.', CAST(4.5 AS Decimal(2, 1)), CAST(N'2026-11-23T11:51:06.0022714' AS DateTime2), N'approved')
INSERT [dbo].[Review] ([ReviewID], [BookingID], [Comment], [Star], [ReviewDate], [ReviewStatus]) VALUES (5, 17, N'Sân hiên có tầm nhìn đẹp, khu vực ăn uống và phòng khách đều rộng rãi.', CAST(5.0 AS Decimal(2, 1)), CAST(N'2026-11-23T11:51:06.0022714' AS DateTime2), N'approved')
SET IDENTITY_INSERT [dbo].[Review] OFF
GO
SET IDENTITY_INSERT [dbo].[Room] ON 

INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (101, 1, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (102, 1, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (103, 2, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (104, 3, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (105, 3, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (106, 4, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (107, 4, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (108, 5, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (110, 1, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (111, 2, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (112, 3, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (113, 4, N'available', 0)
INSERT [dbo].[Room] ([RoomNumber], [RoomTypeID], [Status], [NeedsCleaning]) VALUES (114, 1, N'available', 0)
SET IDENTITY_INSERT [dbo].[Room] OFF
GO
INSERT [dbo].[RoomInventorySettings] ([SettingID], [MaxRooms]) VALUES (1, 13)
GO
SET IDENTITY_INSERT [dbo].[RoomType] ON 

INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive], [MaxChildren]) VALUES (1, N'Deluxe', N'Không gian thanh lịch với nội thất cao cấp, một giường King và cửa sổ lớn nhìn ra thành phố. Phù hợp cho cặp đôi hoặc khách đi công tác.', CAST(60000.00 AS Decimal(18, 2)), 1, 2, N'roomType/room-type-13213634256802690560.jpeg', 1, 1)
INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive], [MaxChildren]) VALUES (2, N'Premier', N'Phòng hướng biển với ban công riêng, giường King và khu vực thư giãn, mang đến kỳ nghỉ yên bình tại Luxe Escape.', CAST(120000.00 AS Decimal(18, 2)), 1, 2, N'roomType/room-type-16325585763120483565.jpeg', 1, 1)
INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive], [MaxChildren]) VALUES (3, N'Executive', N'Kết hợp hài hòa giữa nghỉ ngơi và làm việc, Executive sở hữu không gian sang trọng, bàn làm việc tiện nghi và khu vực tiếp khách thanh lịch. Đặc quyền sử dụng Executive Lounge mang đến sự riêng tư và thuận tiện cho kỳ lưu trú.', CAST(180000.00 AS Decimal(18, 2)), 1, 2, N'roomType/room-type-15610501408038158889.jpeg', 1, 1)
INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive], [MaxChildren]) VALUES (4, N'Grand Family Suite', N'Không gian mở hoàn hảo cho gia đình với phòng khách tách biệt và phòng ngủ riêng tư, đầy đủ tiện nghi giải trí cao cấp nhất.', CAST(12000000.00 AS Decimal(18, 2)), 2, 4, N'roomType/room-type-1646596292133863093.jpeg', 1, 2)
INSERT [dbo].[RoomType] ([RoomTypeID], [Name], [Description], [PricePerNight], [Beds], [Capacity], [Picture], [IsActive], [MaxChildren]) VALUES (5, N'Presidential Suite', N'Hạng phòng cao cấp nhất tại Luxe Escape, gồm hai phòng ngủ, phòng khách lớn, khu vực ăn uống và sân hiên riêng với tầm nhìn toàn cảnh.', CAST(999999.00 AS Decimal(18, 2)), 2, 4, N'roomType/room-type-1312400881820342728.jpeg', 1, 2)
SET IDENTITY_INSERT [dbo].[RoomType] OFF
GO
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 1)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 2)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 3)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 4)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 5)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (1, 6)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 7)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 8)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 9)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 10)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 11)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (2, 12)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 13)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 14)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 15)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 16)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 17)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (3, 18)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 1)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 3)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 5)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 8)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (4, 21)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (5, 9)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (5, 12)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (5, 13)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (5, 18)
INSERT [dbo].[RoomTypeAmenity] ([RoomTypeID], [AmenityID]) VALUES (5, 19)
GO
SET IDENTITY_INSERT [dbo].[User] ON 

INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (1, N'admin', N'pbkdf2$210000$HeMbzGisWFPkHWNYGGDDBg==$UeArxRTwdceh+5eBSREvmIsoJh8n2ObTPA0T8FZ3Gl4=', N'admin', N'Quản Trị', N'active', CAST(N'2026-11-19T21:37:05.9686319' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (2, N'customer', N'pbkdf2$210000$w2txBVjyeZelwAMEt+vcug==$tldgUqI5pi1U4si4aannPudmhMQlJek4NG+QCK3Lijo=', N'customer', N'Khách Hàng', N'active', CAST(N'2026-11-19T21:37:05.9686319' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (3, N'manager', N'pbkdf2$210000$FMBI1HiGaqk/sm+p91powQ==$HR0CG9JofzU7/RyMkGQkIajMfcEtFWHIJLnGIR6IZDQ=', N'manager', N'Quản Lý', N'active', CAST(N'2026-11-19T21:37:05.9686319' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (4, N'staff', N'pbkdf2$210000$MWBzSOyFHyaPXg3qFLOVqg==$JSoJ79whEJgw6jn3POWILnZiHbmMLNMkCVuVR7BBP9Q=', N'staff', N'Nhân Viên', N'active', CAST(N'2026-11-19T21:37:05.9686319' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (11, N'cart_ui_verification', N'pbkdf2$210000$D78L2PgDyk7PAdSf3k/zTw==$jZWDv46TpMV70kgensJp+mEjs3u+3YJTFbbN9QxIEms=', N'customer', N'Kiểm thử giao diện', N'active', CAST(N'2026-11-21T21:41:51.1875526' AS DateTime2))
INSERT [dbo].[User] ([UserID], [Username], [Password], [Role], [DisplayName], [AccountStatus], [CreatedAt]) VALUES (12, N'demo_room_type_reviews', N'3712B158-8009-4B72-8FFE-D963A2977D0F', N'customer', N'Khách mẫu', N'inactive', CAST(N'2026-11-23T11:51:05.9842721' AS DateTime2))
SET IDENTITY_INSERT [dbo].[User] OFF
GO
SET IDENTITY_INSERT [dbo].[Wallet] ON 

INSERT [dbo].[Wallet] ([WalletID], [CustomerID], [Balance]) VALUES (1, 1, CAST(3000 AS Decimal(18, 0)))
SET IDENTITY_INSERT [dbo].[Wallet] OFF
GO
SET IDENTITY_INSERT [dbo].[WalletTransaction] ON 

INSERT [dbo].[WalletTransaction] ([WalletTransactionID], [WalletID], [BookingID], [Amount], [TransactionType], [Description], [CreatedAt]) VALUES (1, 1, 19, CAST(3000 AS Decimal(18, 0)), N'refund', N'Booking cancellation refund', CAST(N'2026-11-23T17:30:21.5203002' AS DateTime2))
SET IDENTITY_INSERT [dbo].[WalletTransaction] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UX_AccountContact_Email]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_AccountContact_Email] ON [dbo].[AccountContact]
(
	[Email] ASC
)
WHERE ([Email] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Amenity__737584F60F998056]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Amenity] ADD UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Amenity__737584F6B2B7D865]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Amenity] ADD UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UX_Booking_OfflineRequestKey]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_Booking_OfflineRequestKey] ON [dbo].[Booking]
(
	[OfflineRequestKey] ASC
)
WHERE ([OfflineRequestKey] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UX_Booking_OnlineRequestKey]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_Booking_OnlineRequestKey] ON [dbo].[Booking]
(
	[CustomerID] ASC,
	[OnlineRequestKey] ASC
)
WHERE ([OnlineRequestKey] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_BookingDetail_TypeStay]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE NONCLUSTERED INDEX [IX_BookingDetail_TypeStay] ON [dbo].[BookingDetail]
(
	[RoomTypeID] ASC,
	[CheckInDate] ASC,
	[CheckOutDate] ASC
)
INCLUDE([BookingID],[RoomNumber]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__CounterQ__9B556A591635C98C]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[CounterQrCheckout] ADD UNIQUE NONCLUSTERED 
(
	[PaymentID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__CounterQ__F6AC419D25C3E577]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[CounterQrCheckout] ADD UNIQUE NONCLUSTERED 
(
	[RequestKey] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UX_CounterQr_ActiveBooking]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_CounterQr_ActiveBooking] ON [dbo].[CounterQrCheckout]
(
	[BookingID] ASC
)
WHERE ([Active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UX_Customer_Email]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_Customer_Email] ON [dbo].[Customer]
(
	[Email] ASC
)
WHERE ([Email] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UX_Customer_LinkedUser]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_Customer_LinkedUser] ON [dbo].[Customer]
(
	[UserID] ASC
)
WHERE ([UserID] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Discount__A25C5AA7C0A3AE57]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Discount] ADD UNIQUE NONCLUSTERED 
(
	[Code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Discount__A25C5AA7EAA29182]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Discount] ADD UNIQUE NONCLUSTERED 
(
	[Code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__OfflineB__F6AC419D0ABA784B]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[OfflineBookingEvent] ADD UNIQUE NONCLUSTERED 
(
	[RequestKey] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__OfflineB__F6AC419D34CC5B70]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[OfflineBookingEvent] ADD UNIQUE NONCLUSTERED 
(
	[RequestKey] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__OfflineC__F6AC419D3913B40F]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[OfflineCharge] ADD UNIQUE NONCLUSTERED 
(
	[RequestKey] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__OfflineC__F6AC419D61A3DC7B]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[OfflineCharge] ADD UNIQUE NONCLUSTERED 
(
	[RequestKey] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__OfflineR__73951ACC0F8A8272]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[OfflineRefund] ADD UNIQUE NONCLUSTERED 
(
	[BookingID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__OfflineR__73951ACCAB9A1567]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[OfflineRefund] ADD UNIQUE NONCLUSTERED 
(
	[BookingID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UX_OfflineRefund_BankReference]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_OfflineRefund_BankReference] ON [dbo].[OfflineRefund]
(
	[Reference] ASC
)
WHERE ([Reference] IS NOT NULL AND [Method]='bank_transfer' AND [Status]='completed')
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Payment__D85E7026101F77D2]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Payment] ADD UNIQUE NONCLUSTERED 
(
	[TransactionCode] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Payment__D85E7026ECCBD7C8]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Payment] ADD UNIQUE NONCLUSTERED 
(
	[TransactionCode] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UX_Payment_CounterBankReference]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_Payment_CounterBankReference] ON [dbo].[Payment]
(
	[CounterReference] ASC
)
WHERE ([CounterReference] IS NOT NULL AND [PaymentMethod]='bank_transfer')
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UX_Payment_CounterRequest]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_Payment_CounterRequest] ON [dbo].[Payment]
(
	[CounterRequestKey] ASC
)
WHERE ([CounterRequestKey] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_Review_Booking]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Review] ADD  CONSTRAINT [UQ_Review_Booking] UNIQUE NONCLUSTERED 
(
	[BookingID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__User__536C85E41497F062]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[User] ADD UNIQUE NONCLUSTERED 
(
	[Username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__User__536C85E46ED30BF8]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[User] ADD UNIQUE NONCLUSTERED 
(
	[Username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__Wallet__A4AE64B958C701AB]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Wallet] ADD UNIQUE NONCLUSTERED 
(
	[CustomerID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__Wallet__A4AE64B97BC0AB91]    Script Date: 11/23/2026 5:35:25 PM ******/
ALTER TABLE [dbo].[Wallet] ADD UNIQUE NONCLUSTERED 
(
	[CustomerID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Withdrawal_Customer_Status]    Script Date: 11/23/2026 5:35:25 PM ******/
CREATE NONCLUSTERED INDEX [IX_Withdrawal_Customer_Status] ON [dbo].[WithdrawalRequest]
(
	[CustomerID] ASC,
	[Status] ASC,
	[CreatedAt] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Amenity] ADD  CONSTRAINT [DF_Amenity_IsActive]  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_BookingDate]  DEFAULT (sysdatetime()) FOR [BookingDate]
GO
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_Status]  DEFAULT ('pending') FOR [Status]
GO
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_TotalPrice]  DEFAULT ((0)) FOR [TotalPrice]
GO
ALTER TABLE [dbo].[Booking] ADD  CONSTRAINT [DF_Booking_OnlineDepositPolicy]  DEFAULT ((0)) FOR [OnlineDepositPolicy]
GO
ALTER TABLE [dbo].[BookingDetail] ADD  CONSTRAINT [DF_BookingDetail_GuestCount]  DEFAULT ((1)) FOR [GuestCount]
GO
ALTER TABLE [dbo].[BookingDetail] ADD  CONSTRAINT [DF_BookingDetail_IsProvisionalRoom]  DEFAULT ((0)) FOR [IsProvisionalRoom]
GO
ALTER TABLE [dbo].[CounterQrCheckout] ADD  DEFAULT ((1)) FOR [Active]
GO
ALTER TABLE [dbo].[CounterQrCheckout] ADD  DEFAULT ('pending') FOR [State]
GO
ALTER TABLE [dbo].[CounterQrCheckout] ADD  DEFAULT ((0)) FOR [LinkAttempted]
GO
ALTER TABLE [dbo].[CounterQrCheckout] ADD  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Discount] ADD  CONSTRAINT [DF_Discount_Quantity]  DEFAULT ((0)) FOR [Quantity]
GO
ALTER TABLE [dbo].[Discount] ADD  CONSTRAINT [DF_Discount_IsActive]  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[OfflineBookingEvent] ADD  DEFAULT ((0)) FOR [Amount]
GO
ALTER TABLE [dbo].[OfflineBookingEvent] ADD  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[OfflineCharge] ADD  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[OfflineRefund] ADD  DEFAULT ('pending') FOR [Status]
GO
ALTER TABLE [dbo].[OfflineRefund] ADD  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Payment] ADD  CONSTRAINT [DF_Payment_Status]  DEFAULT ('pending') FOR [PaymentStatus]
GO
ALTER TABLE [dbo].[Payment] ADD  CONSTRAINT [DF_Payment_RefundedAmount]  DEFAULT ((0)) FOR [RefundedAmount]
GO
ALTER TABLE [dbo].[Review] ADD  CONSTRAINT [DF_Review_ReviewDate]  DEFAULT (sysdatetime()) FOR [ReviewDate]
GO
ALTER TABLE [dbo].[Review] ADD  CONSTRAINT [DF_Review_Status]  DEFAULT ('pending') FOR [ReviewStatus]
GO
ALTER TABLE [dbo].[Room] ADD  CONSTRAINT [DF_Room_Status]  DEFAULT ('available') FOR [Status]
GO
ALTER TABLE [dbo].[Room] ADD  CONSTRAINT [DF_Room_NeedsCleaning]  DEFAULT ((0)) FOR [NeedsCleaning]
GO
ALTER TABLE [dbo].[RoomServiceEvent] ADD  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[RoomType] ADD  CONSTRAINT [DF_RoomType_IsActive]  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[RoomType] ADD  CONSTRAINT [DF_RoomType_MaxChildren]  DEFAULT ((0)) FOR [MaxChildren]
GO
ALTER TABLE [dbo].[User] ADD  CONSTRAINT [DF_User_AccountStatus]  DEFAULT ('active') FOR [AccountStatus]
GO
ALTER TABLE [dbo].[User] ADD  CONSTRAINT [DF_User_CreatedAt]  DEFAULT (sysdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Wallet] ADD  DEFAULT ((0)) FOR [Balance]
GO
ALTER TABLE [dbo].[WalletTransaction] ADD  DEFAULT (sysdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[WithdrawalRequest] ADD  DEFAULT ('pending') FOR [Status]
GO
ALTER TABLE [dbo].[WithdrawalRequest] ADD  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[AccountContact]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[AccountContact]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD FOREIGN KEY([IdentityVerifiedBy])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD FOREIGN KEY([IdentityVerifiedBy])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [FK_Booking_CreatedByUser] FOREIGN KEY([CreatedByUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [FK_Booking_CreatedByUser]
GO
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [FK_Booking_Customer] FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customer] ([CustomerID])
GO
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [FK_Booking_Customer]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD FOREIGN KEY([RoomTypeID])
REFERENCES [dbo].[RoomType] ([RoomTypeID])
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [FK_BookingDetail_Booking] FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [FK_BookingDetail_Booking]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [FK_BookingDetail_Room] FOREIGN KEY([RoomNumber])
REFERENCES [dbo].[Room] ([RoomNumber])
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [FK_BookingDetail_Room]
GO
ALTER TABLE [dbo].[CounterQrCheckout]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[CounterQrCheckout]  WITH CHECK ADD FOREIGN KEY([PaymentID])
REFERENCES [dbo].[Payment] ([PaymentID])
GO
ALTER TABLE [dbo].[CounterQrCheckout]  WITH CHECK ADD FOREIGN KEY([StaffID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Customer]  WITH CHECK ADD  CONSTRAINT [FK_Customer_User] FOREIGN KEY([UserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Customer] CHECK CONSTRAINT [FK_Customer_User]
GO
ALTER TABLE [dbo].[OfflineBookingEvent]  WITH CHECK ADD FOREIGN KEY([ActorUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[OfflineBookingEvent]  WITH CHECK ADD FOREIGN KEY([ActorUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[OfflineBookingEvent]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[OfflineBookingEvent]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[OfflineCharge]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[OfflineCharge]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[OfflineCharge]  WITH CHECK ADD FOREIGN KEY([CreatedByUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[OfflineCharge]  WITH CHECK ADD FOREIGN KEY([CreatedByUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD FOREIGN KEY([PaidByUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD FOREIGN KEY([PaidByUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD FOREIGN KEY([CollectedByUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD FOREIGN KEY([CollectedByUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [FK_Payment_Booking] FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [FK_Payment_Booking]
GO
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [FK_Payment_Discount] FOREIGN KEY([DiscountID])
REFERENCES [dbo].[Discount] ([DiscountID])
GO
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [FK_Payment_Discount]
GO
ALTER TABLE [dbo].[Review]  WITH CHECK ADD  CONSTRAINT [FK_Review_Booking] FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[Review] CHECK CONSTRAINT [FK_Review_Booking]
GO
ALTER TABLE [dbo].[Room]  WITH CHECK ADD  CONSTRAINT [FK_Room_RoomType] FOREIGN KEY([RoomTypeID])
REFERENCES [dbo].[RoomType] ([RoomTypeID])
GO
ALTER TABLE [dbo].[Room] CHECK CONSTRAINT [FK_Room_RoomType]
GO
ALTER TABLE [dbo].[RoomServiceEvent]  WITH CHECK ADD FOREIGN KEY([ActorUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[RoomServiceEvent]  WITH CHECK ADD FOREIGN KEY([ActorUserID])
REFERENCES [dbo].[User] ([UserID])
GO
ALTER TABLE [dbo].[RoomServiceEvent]  WITH CHECK ADD FOREIGN KEY([RoomNumber])
REFERENCES [dbo].[Room] ([RoomNumber])
GO
ALTER TABLE [dbo].[RoomServiceEvent]  WITH CHECK ADD FOREIGN KEY([RoomNumber])
REFERENCES [dbo].[Room] ([RoomNumber])
GO
ALTER TABLE [dbo].[RoomTypeAmenity]  WITH CHECK ADD  CONSTRAINT [FK_RoomTypeAmenity_Amenity] FOREIGN KEY([AmenityID])
REFERENCES [dbo].[Amenity] ([AmenityID])
GO
ALTER TABLE [dbo].[RoomTypeAmenity] CHECK CONSTRAINT [FK_RoomTypeAmenity_Amenity]
GO
ALTER TABLE [dbo].[RoomTypeAmenity]  WITH CHECK ADD  CONSTRAINT [FK_RoomTypeAmenity_RoomType] FOREIGN KEY([RoomTypeID])
REFERENCES [dbo].[RoomType] ([RoomTypeID])
GO
ALTER TABLE [dbo].[RoomTypeAmenity] CHECK CONSTRAINT [FK_RoomTypeAmenity_RoomType]
GO
ALTER TABLE [dbo].[Wallet]  WITH CHECK ADD FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customer] ([CustomerID])
GO
ALTER TABLE [dbo].[Wallet]  WITH CHECK ADD FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customer] ([CustomerID])
GO
ALTER TABLE [dbo].[WalletTransaction]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[WalletTransaction]  WITH CHECK ADD FOREIGN KEY([BookingID])
REFERENCES [dbo].[Booking] ([BookingID])
GO
ALTER TABLE [dbo].[WalletTransaction]  WITH CHECK ADD FOREIGN KEY([WalletID])
REFERENCES [dbo].[Wallet] ([WalletID])
GO
ALTER TABLE [dbo].[WalletTransaction]  WITH CHECK ADD FOREIGN KEY([WalletID])
REFERENCES [dbo].[Wallet] ([WalletID])
GO
ALTER TABLE [dbo].[WithdrawalRequest]  WITH CHECK ADD  CONSTRAINT [FK_Withdrawal_Customer] FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customer] ([CustomerID])
GO
ALTER TABLE [dbo].[WithdrawalRequest] CHECK CONSTRAINT [FK_Withdrawal_Customer]
GO
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [CK_Booking_OnlinePaymentPercent] CHECK  (([OnlinePaymentPercent]=(100) OR [OnlinePaymentPercent]=(50)))
GO
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [CK_Booking_OnlinePaymentPercent]
GO
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [CK_Booking_Status] CHECK  (([Status]='completed' OR [Status]='checked_out' OR [Status]='checked_in' OR [Status]='cancelled' OR [Status]='confirmed' OR [Status]='pending'))
GO
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [CK_Booking_Status]
GO
ALTER TABLE [dbo].[Booking]  WITH CHECK ADD  CONSTRAINT [CK_Booking_TotalPrice] CHECK  (([TotalPrice]>=(0)))
GO
ALTER TABLE [dbo].[Booking] CHECK CONSTRAINT [CK_Booking_TotalPrice]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_ActualGuestCount] CHECK  (([ActualGuestCount]>=(1) AND [ActualGuestCount]<=(1000)))
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_ActualGuestCount]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_BookedParty] CHECK  (([BookedAdults] IS NULL AND [BookedChildren] IS NULL OR [BookedAdults] IS NOT NULL AND [BookedChildren] IS NOT NULL AND [BookedAdults]>(0) AND [BookedChildren]>=(0) AND [GuestCount]=([BookedAdults]+[BookedChildren])))
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_BookedParty]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_Date] CHECK  (([CheckOutDate]>[CheckInDate]))
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_Date]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_GuestCount] CHECK  (([GuestCount]>(0)))
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_GuestCount]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_Price] CHECK  (([PricePerNight]>=(0)))
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_Price]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_ProvisionalRoom] CHECK  (([IsProvisionalRoom]=(0) OR [RoomNumber] IS NOT NULL))
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_ProvisionalRoom]
GO
ALTER TABLE [dbo].[BookingDetail]  WITH CHECK ADD  CONSTRAINT [CK_BookingDetail_RoomOrType] CHECK  (([RoomNumber] IS NOT NULL OR [RoomTypeID] IS NOT NULL))
GO
ALTER TABLE [dbo].[BookingDetail] CHECK CONSTRAINT [CK_BookingDetail_RoomOrType]
GO
ALTER TABLE [dbo].[CounterQrCheckout]  WITH CHECK ADD  CONSTRAINT [CK_CounterQr_Order] CHECK  (([OrderCode]>=(900000000000000.) AND [OrderCode]<(1000000000000000.)))
GO
ALTER TABLE [dbo].[CounterQrCheckout] CHECK CONSTRAINT [CK_CounterQr_Order]
GO
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_Date] CHECK  (([EndDate] IS NULL OR [StartDate] IS NULL OR [EndDate]>=[StartDate]))
GO
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_Date]
GO
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_MaximumDiscount] CHECK  (([MaximumDiscount] IS NULL OR [MaximumDiscount]>=(0)))
GO
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_MaximumDiscount]
GO
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_MinimumAmount] CHECK  (([MinimumAmount] IS NULL OR [MinimumAmount]>=(0)))
GO
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_MinimumAmount]
GO
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_Quantity] CHECK  (([Quantity]>=(0)))
GO
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_Quantity]
GO
ALTER TABLE [dbo].[Discount]  WITH CHECK ADD  CONSTRAINT [CK_Discount_SaleOff] CHECK  (([SaleOff]>=(0) AND [SaleOff]<=(100)))
GO
ALTER TABLE [dbo].[Discount] CHECK CONSTRAINT [CK_Discount_SaleOff]
GO
ALTER TABLE [dbo].[OfflineCharge]  WITH CHECK ADD CHECK  (([Amount]>(0)))
GO
ALTER TABLE [dbo].[OfflineCharge]  WITH CHECK ADD CHECK  (([Amount]>(0)))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Amount]>(0)))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Amount]>(0)))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Method]='bank_transfer' OR [Method]='cash'))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Method]='bank_transfer' OR [Method]='cash'))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Status]='completed' OR [Status]='pending'))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Status]='completed' OR [Status]='pending'))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Fee]>=(0)))
GO
ALTER TABLE [dbo].[OfflineRefund]  WITH CHECK ADD CHECK  (([Fee]>=(0)))
GO
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [CK_Payment_Amount] CHECK  (([Amount]>=(0)))
GO
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [CK_Payment_Amount]
GO
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [CK_Payment_Method] CHECK  (([PaymentMethod]='e_wallet' OR [PaymentMethod]='credit_card' OR [PaymentMethod]='bank_transfer' OR [PaymentMethod]='cash'))
GO
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [CK_Payment_Method]
GO
ALTER TABLE [dbo].[Payment]  WITH CHECK ADD  CONSTRAINT [CK_Payment_Status] CHECK  (([PaymentStatus]='refunded' OR [PaymentStatus]='failed' OR [PaymentStatus]='paid' OR [PaymentStatus]='pending'))
GO
ALTER TABLE [dbo].[Payment] CHECK CONSTRAINT [CK_Payment_Status]
GO
ALTER TABLE [dbo].[Review]  WITH CHECK ADD  CONSTRAINT [CK_Review_Star] CHECK  (([Star]>=(1) AND [Star]<=(5)))
GO
ALTER TABLE [dbo].[Review] CHECK CONSTRAINT [CK_Review_Star]
GO
ALTER TABLE [dbo].[Review]  WITH CHECK ADD  CONSTRAINT [CK_Review_Status] CHECK  (([ReviewStatus]='rejected' OR [ReviewStatus]='approved' OR [ReviewStatus]='pending'))
GO
ALTER TABLE [dbo].[Review] CHECK CONSTRAINT [CK_Review_Status]
GO
ALTER TABLE [dbo].[Room]  WITH CHECK ADD  CONSTRAINT [CK_Room_Status] CHECK  (([Status]='out_of_service' OR [Status]='maintenance' OR [Status]='available'))
GO
ALTER TABLE [dbo].[Room] CHECK CONSTRAINT [CK_Room_Status]
GO
ALTER TABLE [dbo].[RoomInventorySettings]  WITH CHECK ADD  CONSTRAINT [CK_RoomInventorySettings_MaxRooms] CHECK  (([MaxRooms]>(0)))
GO
ALTER TABLE [dbo].[RoomInventorySettings] CHECK CONSTRAINT [CK_RoomInventorySettings_MaxRooms]
GO
ALTER TABLE [dbo].[RoomInventorySettings]  WITH CHECK ADD  CONSTRAINT [CK_RoomInventorySettings_Singleton] CHECK  (([SettingID]=(1)))
GO
ALTER TABLE [dbo].[RoomInventorySettings] CHECK CONSTRAINT [CK_RoomInventorySettings_Singleton]
GO
ALTER TABLE [dbo].[RoomType]  WITH CHECK ADD  CONSTRAINT [CK_RoomType_Beds] CHECK  (([Beds]>(0)))
GO
ALTER TABLE [dbo].[RoomType] CHECK CONSTRAINT [CK_RoomType_Beds]
GO
ALTER TABLE [dbo].[RoomType]  WITH CHECK ADD  CONSTRAINT [CK_RoomType_Capacity] CHECK  (([Capacity]>(0)))
GO
ALTER TABLE [dbo].[RoomType] CHECK CONSTRAINT [CK_RoomType_Capacity]
GO
ALTER TABLE [dbo].[RoomType]  WITH CHECK ADD  CONSTRAINT [CK_RoomType_MaxChildren] CHECK  (([MaxChildren]>=(0)))
GO
ALTER TABLE [dbo].[RoomType] CHECK CONSTRAINT [CK_RoomType_MaxChildren]
GO
ALTER TABLE [dbo].[RoomType]  WITH CHECK ADD  CONSTRAINT [CK_RoomType_Price] CHECK  (([PricePerNight]>=(0)))
GO
ALTER TABLE [dbo].[RoomType] CHECK CONSTRAINT [CK_RoomType_Price]
GO
ALTER TABLE [dbo].[User]  WITH CHECK ADD  CONSTRAINT [CK_User_AccountStatus] CHECK  (([AccountStatus]='inactive' OR [AccountStatus]='locked' OR [AccountStatus]='active'))
GO
ALTER TABLE [dbo].[User] CHECK CONSTRAINT [CK_User_AccountStatus]
GO
ALTER TABLE [dbo].[User]  WITH CHECK ADD  CONSTRAINT [CK_User_Role] CHECK  (([Role]='it_support' OR [Role]='manager' OR [Role]='admin' OR [Role]='staff' OR [Role]='customer'))
GO
ALTER TABLE [dbo].[User] CHECK CONSTRAINT [CK_User_Role]
GO
ALTER TABLE [dbo].[WithdrawalRequest]  WITH CHECK ADD  CONSTRAINT [CK_Withdrawal_Amount] CHECK  (([Amount]>(0)))
GO
ALTER TABLE [dbo].[WithdrawalRequest] CHECK CONSTRAINT [CK_Withdrawal_Amount]
GO
ALTER TABLE [dbo].[WithdrawalRequest]  WITH CHECK ADD  CONSTRAINT [CK_Withdrawal_Status] CHECK  (([Status]='paid' OR [Status]='rejected' OR [Status]='approved' OR [Status]='pending'))
GO
ALTER TABLE [dbo].[WithdrawalRequest] CHECK CONSTRAINT [CK_Withdrawal_Status]
GO
/****** Object:  StoredProcedure [dbo].[ExpireOfflineBookingHolds]    Script Date: 11/23/2026 5:35:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[ExpireOfflineBookingHolds] AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        ;WITH due AS (
            SELECT TOP(100) b.* FROM dbo.Booking b WITH(UPDLOCK,READPAST,ROWLOCK)
            WHERE b.CreatedByUserID IS NOT NULL AND b.Status='pending'
                AND b.HoldExpiresAt<=SYSUTCDATETIME()
                AND NOT EXISTS (
                    SELECT 1 FROM dbo.CounterQrCheckout q WITH(HOLDLOCK)
                    WHERE q.BookingID=b.BookingID AND q.Active=1
                )
            ORDER BY b.HoldExpiresAt,b.BookingID
        )
        UPDATE due SET Status='cancelled',CancellationDate=SYSDATETIME(),
            CancellationReason=N'Hết hạn thanh toán'
        WHERE NOT EXISTS (
            SELECT 1 FROM dbo.Payment p WITH(HOLDLOCK)
            WHERE p.BookingID=due.BookingID AND p.PaymentStatus='paid'
        );
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
EXEC sys.sp_addextendedproperty @name=N'BookingDetailRoomTypeSchemaBefore', @value=N'main' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'BookingDetail'
GO
USE [master]
GO
ALTER DATABASE [HotelDB] SET  READ_WRITE 
GO
