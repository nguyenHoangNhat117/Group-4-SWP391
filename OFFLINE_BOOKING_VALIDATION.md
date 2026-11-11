# Kết quả kiểm tra bản sửa offline booking

Ngày kiểm tra: 11/11/2026. Nền source: `f2766c0`.

## Môi trường

- OpenJDK 21, build target Java 11.
- Maven 3.9.11.
- Tomcat 10.1.48.
- SQL Server trong container local; database kiểm thử tách riêng khỏi HotelDB hiện có.
- Chromium chạy tự động bằng Playwright, kiểm tra desktop 1440 px và mobile 390 px.

## Đã kiểm tra

- Build WAR thành công.
- SQL migration áp dụng thành công và chạy lại được.
- Bộ `OfflineWorkflowIT`: 43 kiểm tra tích hợp với SQL Server, bao gồm:
  - Tạo khách không email/không tài khoản; tìm lại hồ sơ; quản lý vẫn thấy booking.
  - Gửi lại yêu cầu tạo đơn không sinh đơn thứ hai.
  - Chặn trùng lịch; cho phép ngày trả của đơn trước trùng ngày nhận của đơn sau.
  - Thu cọc, thu số dư; gửi lại cùng mã yêu cầu không thu hai lần.
  - Hai nhân viên đồng thời thu trên cùng snapshot: chỉ một yêu cầu được ghi nhận.
  - Snapshot cũ, thiếu mã chuyển khoản, tái sử dụng mã chuyển khoản bị từ chối và rollback.
  - Cọc tùy chỉnh 25%, voucher 10% chỉ trừ một lần, voucher 100% xác nhận đơn 0 đồng.
  - Kiểm tra giấy tờ, sức chứa, ngày nhận và phòng chờ dọn khi nhận phòng.
  - Chặn trả phòng còn nợ; thu phát sinh, trả phòng, hoàn tất và xác nhận dọn phòng.
  - Không cho đường đổi trạng thái cũ bỏ qua workflow offline.
  - Hủy trước ngày nhận, hủy trong ngày/no-show, tạo khoản chờ hoàn và phân bổ khi đã chi.
  - Gửi lại xác nhận hoàn không hoàn hai lần.
  - Hết hạn dựa theo HoldExpiresAt, không bị cơ chế 30 phút chung hủy sớm.
  - Booking đã cọc không bị hạn giữ phòng cũ hủy.
  - Quyền staff bị khóa được kiểm tra lại ngay trong transaction.
- Luồng thực trên trình duyệt: đăng nhập admin, tìm phòng, tạo hồ sơ khách mới không email,
  tạo booking, thu cọc, nhận phòng, thử trả khi còn nợ (bị chặn), thu số dư,
  trả phòng, hoàn tất và xác nhận dọn phòng; mở hồ sơ khách không tài khoản; mở Hotel Wallet.
- Lượt kiểm tra trình duyệt không ghi nhận lỗi JavaScript; layout counter không tràn ngang ở 390 px.

## Phạm vi xác nhận

Các kiểm tra này không thay thế nghiệm thu trên máy triển khai của nhóm.
Chưa kiểm tra gửi mail/PayOS live, chuyển tiền ngân hàng thực, máy in, hóa đơn điện tử,
khai báo lưu trú, khả năng chịu tải nhiều chi nhánh hoặc mọi cấu hình SQL Server tùy biến.
Trước khi dùng dữ liệu thật, chạy lại migration và bộ test trên database staging,
kiểm tra tài khoản staff/admin, đối chiếu số dư, và xác nhận chính sách cọc/hủy với khách sạn.
