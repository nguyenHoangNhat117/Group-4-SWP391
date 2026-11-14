# Thu phần còn lại tại quầy cho đơn online

Áp dụng cho đơn online sử dụng chính sách cọc 50% hoặc thanh toán đủ qua payOS. Trang chi tiết đơn hiển thị **Nhận phòng**, **Thu phần còn lại**, **Trả phòng** và xác nhận dọn theo trạng thái. Khách có thể trả số còn thiếu bằng tiền mặt hoặc QR trước hoặc sau khi nhận phòng. Khi trả phòng và đã quyết toán đủ, đơn online chuyển thẳng sang **Hoàn tất**, lưu giờ trả thực tế và đánh dấu phòng chờ dọn. Đơn offline tiếp tục dùng màn tại quầy và thao tác hoàn tất hiện có.

## Các file liên quan đến QR

| File | Thay đổi |
| --- | --- |
| `src/main/java/dao/CounterQrDAO.java` | Tạo và đối soát QR thu số dư cho đơn online đã xác nhận hoặc đã nhận phòng. Truy vấn QR trả thêm thông tin nguồn đặt để điều hướng về đúng màn sau thanh toán. |
| `src/main/java/controller/CounterPaymentServlet.java` | Dùng chung báo giá và mã yêu cầu từ trang chi tiết hoặc màn tại quầy. Đơn online quay lại chi tiết sau thu tiền mặt hoặc sau khi rời màn QR. |
| `src/main/java/controller/BookingManagementServlet.java` | Nạp số dư và QR chờ đối soát để lễ tân thao tác trực tiếp trên chi tiết đơn online. |
| `src/main/java/dao/OfflineBookingDAO.java` | Cung cấp báo giá cho màn thu tại quầy của đơn online; đọc số đã thu sau hoàn tiền và xử lý hạn giữ phòng có thể trống. |
| `src/main/java/dao/OfflineOperationsDAO.java` | Chặn thao tác tài chính khi có giao dịch chờ đối soát. Khi trả phòng, ghi giờ thực tế, đánh dấu chờ dọn và hoàn tất đơn online trong cùng transaction. |
| `src/main/java/dao/PaymentDAO.java` | Tính số đã thu sau hoàn tiền cho kiểm tra đã thanh toán và hiển thị công nợ. |
| `src/main/webapp/WEB-INF/booking-management/detail.jsp` và `online-actions.jspf` | Hiển thị form thu tiền mặt hoặc tạo/mở lại QR ngay trên trang chi tiết đơn online. |
| `src/main/webapp/WEB-INF/booking-management/counter-qr.jsp` | Dùng đường dẫn do server cung cấp để quay về đúng chi tiết đơn online hoặc màn tại quầy của đơn offline. |
| `src/main/webapp/assets/js/offline-counter.js` | Dùng chung xử lý form thu tiền trên hai màn và xác nhận hủy đơn khi bấm nút hủy. |

Ứng dụng dùng migration `src/main/resources/db/counter_qr.sql` đã có của UC09, gồm bảng, trigger và thủ tục hết hạn giữ phòng. Khi chuẩn bị một database mới, áp dụng migration đó sau schema gốc. Mã xử lý webhook payOS, hàm ký yêu cầu, cấu hình cổng thanh toán và script hiển thị QR được giữ theo bản UC09.

## Kiểm tra trên web

1. Đặt hai đơn online đã cọc 50%. Tại quầy, thu số dư của đơn thứ nhất bằng tiền mặt và của đơn thứ hai bằng QR payOS. Mỗi đơn phải có hai giao dịch và số dư bằng 0.
2. Với một đơn còn thiếu tiền, thử trả phòng. Lặp lại khi QR đang chờ. Sau khi payOS xác nhận QR, thử lại thao tác trả phòng và xác nhận phòng ở trạng thái chờ dọn.
3. Mở đơn đã trả 100% online, kiểm tra trang chi tiết không hiển thị form thu. Trả phòng và kiểm tra đơn đã hoàn tất, rồi xác nhận dọn.
4. Kiểm tra lại một đơn tạo tại quầy thanh toán cọc, quyết toán và trả phòng.

Kiểm thử tích hợp trên SQL Server tạm với API payOS mô phỏng đã xác minh nhận phòng sau khi cọc 50%, thu tiền mặt, mở lại QR đang chờ, đối soát thành công, xử lý QR hết hạn, chặn thu thêm cho đơn đã trả đủ, trả phòng, xác nhận dọn và thu cọc cho đơn offline. Còn cần kiểm tra thao tác trên web với payOS của môi trường triển khai.
