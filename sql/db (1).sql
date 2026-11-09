USE HotelDB;
GO

/* ============================================================
   1. CẬP NHẬT BẢNG [User] (Mục 2)
   - Bỏ role 'it_support'
   - Thêm cột Email và Phone
   - Cài đặt UNIQUE cho Email (nhưng cho phép nhiều dòng NULL)
   ============================================================ */
-- Xóa ràng buộc Role cũ
ALTER TABLE [User] DROP CONSTRAINT CK_User_Role;
GO
-- Thêm ràng buộc Role mới
ALTER TABLE [User] ADD CONSTRAINT CK_User_Role 
    CHECK ([Role] IN ('customer', 'staff', 'admin', 'manager'));
GO
-- Thêm cột Email và Phone
ALTER TABLE [User] ADD 
    Email NVARCHAR(100) NULL,
    Phone NVARCHAR(20) NULL;
GO
-- Đảm bảo Email là duy nhất nếu có nhập (cho phép nhiều giá trị NULL)
CREATE UNIQUE NONCLUSTERED INDEX UQ_User_Email 
ON [User](Email) 
WHERE Email IS NOT NULL;
GO

/* ============================================================
   2. CẬP NHẬT BẢNG [Booking] (Mục 3 & Mục 4)
   - Thêm DiscountID (tham chiếu bảng Discount)
   - Thêm PaymentStatus độc lập
   ============================================================ */
ALTER TABLE Booking ADD 
    DiscountID INT NULL,
    PaymentStatus NVARCHAR(25) NOT NULL 
        CONSTRAINT DF_Booking_PaymentStatus DEFAULT 'unpaid';
GO

-- Khóa ngoại cho DiscountID
ALTER TABLE Booking ADD CONSTRAINT FK_Booking_Discount 
    FOREIGN KEY (DiscountID) REFERENCES Discount(DiscountID);
GO

-- Ràng buộc giá trị cho PaymentStatus
ALTER TABLE Booking ADD CONSTRAINT CK_Booking_PaymentStatus 
    CHECK (PaymentStatus IN ('unpaid', 'pending', 'partially_paid', 'paid', 'refunded', 'partially_refunded'));
GO

/* ============================================================
   3. CẬP NHẬT BẢNG [Payment] (Mục 5)
   - Thêm cột Note lưu ghi chú/lý do
   - Bỏ ràng buộc chặn số âm của Amount để dễ lưu tiền hoàn trả
   ============================================================ */
ALTER TABLE Payment ADD Note NVARCHAR(255) NULL;
GO

ALTER TABLE Payment DROP CONSTRAINT CK_Payment_Amount;
GO

/* ============================================================
   4. CẬP NHẬT BẢNG [RoomType] (Mục 7)
   - Thêm UNIQUE cho cột Name
   ============================================================ */
ALTER TABLE RoomType ADD CONSTRAINT UQ_RoomType_Name UNIQUE ([Name]);
GO

/* ============================================================
   5. XỬ LÝ BỎ IDENTITY CỘT RoomNumber Ở BẢNG [Room] (Mục 6 & 8)
   Dùng kỹ thuật bảng tạm để bỏ IDENTITY mà không làm mất dữ liệu 
   hay phải đổi kiểu dữ liệu hàng loạt.
   ============================================================ */
-- B1: Xóa khóa ngoại từ bảng BookingDetail đang trỏ tới Room
ALTER TABLE BookingDetail DROP CONSTRAINT FK_BookingDetail_Room;
GO

-- B2: Tạo bảng Room mới y hệt nhưng KHÔNG CÓ IDENTITY ở RoomNumber
CREATE TABLE Room_New (
    RoomNumber INT PRIMARY KEY,
    RoomTypeID INT NOT NULL,
    [Status] NVARCHAR(20) NOT NULL CONSTRAINT DF_Room_Status_New DEFAULT 'available',
    CONSTRAINT CK_Room_Status_New CHECK ([Status] IN ('available', 'maintenance', 'out_of_service')),
    CONSTRAINT FK_Room_RoomType_New FOREIGN KEY (RoomTypeID) REFERENCES RoomType(RoomTypeID)
);
GO

-- B3: Đổ dữ liệu từ bảng Room cũ sang bảng mới
INSERT INTO Room_New (RoomNumber, RoomTypeID, [Status])
SELECT RoomNumber, RoomTypeID, [Status] FROM Room;
GO

-- B4: Xóa hẳn bảng Room cũ
DROP TABLE Room;
GO

-- B5: Đổi tên bảng mới lại thành Room
EXEC sp_rename 'Room_New', 'Room';
GO

-- B6: Đổi tên lại các Constraint cho chuẩn mực
EXEC sp_rename 'DF_Room_Status_New', 'DF_Room_Status', 'OBJECT';
EXEC sp_rename 'CK_Room_Status_New', 'CK_Room_Status', 'OBJECT';
EXEC sp_rename 'FK_Room_RoomType_New', 'FK_Room_RoomType', 'OBJECT';
GO

-- B7: Nối lại Khóa ngoại cho bảng BookingDetail
ALTER TABLE BookingDetail ADD CONSTRAINT FK_BookingDetail_Room 
    FOREIGN KEY (RoomNumber) REFERENCES Room(RoomNumber);
GO

PRINT 'Cap nhat Database thanh cong!';
GO