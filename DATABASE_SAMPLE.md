# Dữ liệu mẫu HotelDB

## Cách chạy

1. Nếu chưa có database, chạy `HotelDB.sql` trong SSMS. Không chạy lại file tạo database trên database đang tồn tại.
2. Chạy `HotelDB.seed.sql`. Script tự chọn `HotelDB`; bảng kết quả cuối hiển thị ID thật, phòng, ngày và tổng tiền của từng booking.

```powershell
sqlcmd -S '.\SQLEXPRESS' -E -C -b -i HotelDB.seed.sql -f 65001
```

Seed dành cho môi trường phát triển và schema hiện tại trong `HotelDB.sql`. Không xóa dữ liệu cũ, không giả định ID bắt đầu từ 1, không tắt khóa ngoại. Mọi insert nằm trong một transaction; lỗi sẽ rollback. Nếu có username `seed.*`, script dừng để tránh nhân đôi hay ghi đè. Không chạy lại để làm mới ngày.

## Tài khoản

Mật khẩu chung: **`HotelDemo@123`**. Lưu bằng PBKDF2-HMAC-SHA256, 210.000 vòng, đúng định dạng `PasswordUtil`.

| Username | Vai trò |
|---|---|
| seed.admin | admin |
| seed.manager | manager |
| seed.staff | staff |
| seed.it | it_support |
| seed.an, seed.binh, seed.chi, seed.dung | customer |

Email dùng miền `example.com`; tên, điện thoại và ngân hàng là dữ liệu giả. Không dùng yêu cầu rút tiền mẫu để chuyển tiền thực.

## Phạm vi

8 tài khoản và liên hệ, 4 khách hàng và ví, 5 loại phòng, 20 phòng, thông số phòng, 4 tiện nghi, 20 liên kết tiện nghi, 3 mã giảm giá, 10 booking, 10 chi tiết phòng, 10 thanh toán, 3 đánh giá; kèm phụ thu, hoàn tiền, lịch sử ví, yêu cầu rút tiền và nhật ký vận hành.

Giá phòng theo catalog hiện tại: 2,5 / 4 / 6,5 / 12 / 50 triệu VND mỗi đêm. Loại phòng mẫu có tiền tố `Demo`; ảnh trỏ đến file có thật trong project. Không cần chạy `room_catalog.sql` cho seed; migration đó quản lý catalog riêng.

`D` là ngày chạy theo múi giờ Việt Nam. Thời gian lưu trú dùng giờ Việt Nam; bảng vận hành theo UTC được chuyển tương ứng.

| STT | Khách | Trạng thái | Ngày ở | Tổng tiền VND | Tình huống |
|---|---|---|---|---:|---|
| 1 | an | completed | D−60 → D−58 | 4.500.000 | Online, giảm 10% từ 5 triệu, trả đủ, đánh giá duyệt |
| 2 | binh | completed | D−30 → D−27 | 12.000.000 | Online, trả đủ, đánh giá duyệt |
| 3 | chi | completed | D−10 → D−8 | 24.300.000 | Tại quầy, 4 khách, phụ thu 300.000, đánh giá chờ duyệt |
| 4 | dung | checked_in | D−2 → D+3 | 12.500.000 | Đã nhận phòng, xác minh khách, trả đủ |
| 5 | an | confirmed | D+3 → D+5 | 8.000.000 | Online, cọc 4 triệu, còn 4 triệu |
| 6 | binh | pending | D+10 → D+12 | 5.000.000 | Online, chưa thanh toán |
| 7 | chi | confirmed | D+7 → D+10 | 36.000.000 | Tại quầy, cọc 18 triệu, còn 18 triệu |
| 8 | dung | cancelled | D+20 → D+22 | 8.000.000 | Online, hủy trước hơn 7 ngày, hoàn đủ vào ví |
| 9 | binh | cancelled | D+15 → D+17 | 5.000.000 | Tại quầy, hoàn đủ tiền mặt, phí 0 |
| 10 | an | checked_out | D−4 → D−2 | 8.000.000 | Trả phòng, chờ dọn |

Ví `seed.dung` có 8 triệu từ booking 8 và yêu cầu rút 2 triệu đang chờ. Số dư lưu vẫn là 8 triệu vì DAO chỉ trừ khi chi trả. Ví khác bằng 0. Hoàn toàn bộ online dùng `PaymentStatus=refunded`, `RefundedAmount=0` theo `BookingDAO`; hoàn tại quầy ghi số đã hoàn vào `RefundedAmount` theo `OfflineOperationsDAO`.

Phòng đang có khách vẫn có `Room.Status=available`: ứng dụng tính chiếm phòng qua booking. Một phòng khác đang `maintenance`. `SEED10` còn 99 lượt sau một lần dùng; `SEED_EXPIRED` hết hạn; `SEED_DISABLED` vô hiệu hóa.

## Kiểm tra

Seed kiểm tra tổng tiền = tiền phòng − giảm giá + phụ thu, sức chứa, không trùng lịch, thanh toán đủ cho lượt đã nhận/trả phòng, số dư ví bằng lịch sử. SQL Server kiểm tra FK, UNIQUE và CHECK.

```powershell
./scripts/Test-DatabaseSeed.ps1 -Server '.\SQLEXPRESS'
```

Kiểm thử cần quyền tạo database: tạo database ngẫu nhiên `HotelSeedVerify_*`, nạp schema và seed, kiểm tra lần chạy thứ hai bị chặn, rồi xóa chính database tạm đó. Không nạp vào `HotelDB` của ứng dụng.
