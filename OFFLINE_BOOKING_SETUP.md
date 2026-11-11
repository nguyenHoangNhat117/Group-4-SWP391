# Bản cập nhật đặt phòng tại quầy

Đọc file này trước khi chạy bản mới. Code được phát triển từ `f2766c0` của Group-4-SWP391.
Đây là source đầy đủ của project, không phải các đoạn code cần ghép bằng tay.

## 1. Những chức năng đã hoàn thiện

- Tìm phòng theo ngày và sức chứa; kiểm tra lại phòng, giá và khuyến mãi khi tạo.
- Tạo hồ sơ khách tại quầy mà không tạo tài khoản website hoặc mật khẩu ngẫu nhiên.
- Email khách có thể để trống; điện thoại 7–15 chữ số, hỗ trợ dấu `+` và chuẩn hóa khoảng trắng/dấu gạch/ngoặc.
- Có thể tìm lại và chỉnh sửa hồ sơ khách tại quầy trong Customers.
- Tạo đơn giữ phòng, mặc định 30 phút; chống tạo đơn trùng khi gửi lại cùng yêu cầu.
- Thu cọc theo chính sách của từng booking hoặc thu toàn bộ số dư. Mặc định cọc 20%.
- Xác nhận booking sau khi thu đủ cọc; lưu phiếu thu và nhân viên thực hiện trong một transaction.
- Chống thu trùng khi gửi lại yêu cầu, bấm nhiều lần hoặc nhiều nhân viên thao tác đồng thời.
- Thu chuyển khoản thủ công phải nhập mã giao dịch đã đối chiếu. Mã này không được sử dụng cho hai phiếu thu.
- Chặn nhận phòng trước/trễ ngày đã đặt, chưa đủ cọc, vượt sức chứa, phòng bẩn/bảo trì hoặc phòng còn khách.
- Xác nhận đã đối chiếu giấy tờ khi nhận phòng; lưu người kiểm tra, số khách thực tế, thời điểm nhận/trả theo UTC.
- Thêm khoản phát sinh có nội dung và lịch sử; chỉ được thêm trước khi trả phòng.
- Chặn trả phòng khi còn công nợ. Trả phòng xong chuyển phòng sang chờ dọn; xác nhận dọn sau khi kiểm tra thực tế.
- Hủy/no-show trước nhận phòng, có lý do và quy tắc phí rõ ràng.
- Ghi khoản cần hoàn ở trạng thái chờ; chỉ xác nhận đã hoàn sau khi thực chi tiền mặt hoặc chuyển khoản.
- Phân bổ tiền hoàn về phiếu thu gốc; không tự chuyển tiền mặt của khách vào ví website.
- Cập nhật tổng tiền hoàn ở Hotel Wallet để bao gồm khoản hoàn tại quầy đã thực chi.
- Chặn các đường đổi trạng thái/thanh toán cũ đối với booking offline để không bỏ qua quy trình mới.
- Thanh bước được sửa để nhãn dịch không bị bó vào hình tròn; màn thao tác tại quầy hỗ trợ desktop/mobile.

## 2. Yêu cầu chạy

- JDK 11 trở lên để build; khuyến nghị JDK 17 hoặc 21.
- Maven 3.9 trở lên.
- Tomcat 10.1 (Jakarta), không dùng Tomcat 9.
- SQL Server và schema tương ứng với source `main` tại `f2766c0`.

## 3. Cập nhật database đang có

1. Sao lưu database và dừng Tomcat để không có thao tác ghi trong lúc nâng cấp.
2. Mở SSMS, chọn đúng database của ứng dụng trong danh sách database.
3. Chạy toàn bộ `sql/UC09_StableWorkflow.sql`. File không hardcode tên database và không xóa dữ liệu.
4. Nếu script báo thiếu AccountContact/HoldExpiresAt, cần hoàn tất các migration UC18 và UC09 trước. Không bỏ qua lỗi rồi chạy ứng dụng.
5. Kiểm tra script kết thúc không có lỗi, sau đó triển khai WAR mới.

Migration bổ sung bảng lịch sử, phiếu hoàn, phát sinh, tình trạng chờ dọn, người thu tiền và khóa chống trùng.
Customer.UserID/Email được phép NULL; liên kết tài khoản và email có giá trị vẫn được bảo vệ bằng unique index.
Nếu database cũ đang có email khách trùng, unique index sẽ báo lỗi: đối soát hồ sơ trùng trước khi chạy lại, không tự xóa hồ sơ hoặc giao dịch.
Script có thể chạy lại; đã kiểm tra bằng cách áp dụng hai lần trên SQL Server.

Không chạy lại `HotelDB.sql` hoặc `script_fixed.sql` trên database đang sử dụng.
Không sửa trạng thái booking trực tiếp trong SSMS để vượt qua bước thu tiền.

## 4. Nếu chạy trên database trống

1. Chạy `script_fixed.sql` trong SSMS để tạo schema nền và dữ liệu demo.
2. Chọn database vừa tạo, chạy `sql/UC09_StableWorkflow.sql`.
3. Thay thông tin khách sạn, dữ liệu demo và tài khoản demo trước khi sử dụng thực tế.

## 5. Cấu hình kết nối

Ứng dụng đọc ba biến môi trường của tiến trình Tomcat (hoặc Java System Properties cùng tên):

| Biến | Nội dung |
|---|---|
| DB_URL | JDBC URL SQL Server, kèm tên database, ví dụ `jdbc:sqlserver://localhost:1433;databaseName=HotelDB;encrypt=true` |
| DB_USER | Tài khoản SQL Server có quyền cần thiết trên database ứng dụng |
| DB_PASSWORD | Mật khẩu SQL Server |

Biến môi trường phải được truyền cho Tomcat/NetBeans, không chỉ đặt trong terminal khác.
Code không tự đọc `.env`. Sau khi cấu hình, thoát NetBeans và mở lại nếu dùng NetBeans để chạy server.
Khi chưa khai báo các biến trên, code giữ cấu hình localhost cũ để tương thích; khi triển khai hãy đặt rõ cả ba biến.
Chứng chỉ TLS phải phù hợp với máy SQL Server; kết nối kiểm thử local trong báo cáo dùng `encrypt=false` trên loopback, không dùng cấu hình đó làm mặc định cho máy chủ từ xa.

Không cần SMTP hoặc PayOS để thu tiền mặt/ghi nhận chuyển khoản tại quầy.
SMTP/PayOS của các chức năng khác vẫn cấu hình riêng như trước.

Có thể chỉnh chính sách mặc định qua VM Options của Tomcat:

```text
-Duc09.holdMinutes=30 -Duc09.depositPercent=20
```

Thay đổi chỉ áp dụng cho booking mới; chính sách đã lưu trên booking cũ được giữ nguyên.
Giữ phòng: 1–1440 phút; cọc: 1–100%. Tiền cọc làm tròn lên theo VND để không thu thiếu mức tối thiểu.

## 6. Build và triển khai

Từ thư mục gốc của project:

```text
mvn -f HotelPage/pom.xml clean package
```

WAR được tạo ở `HotelPage/target/HotelPage-1.0-SNAPSHOT.war`.
Triển khai lên Tomcat 10.1, khởi động lại rồi đăng nhập tài khoản staff/admin.
Vào Offline Booking để tạo đơn; sau khi tạo, màn Counter Payment là nơi thao tác toàn bộ vòng đời đơn.
Trong Booking Management, mở chi tiết đơn offline rồi chọn nút thao tác tại quầy để quay lại màn này.

Không chép riêng DAO mới vào ứng dụng cũ. Cần triển khai đồng bộ Java, JSP, CSS, JS, resources và SQL migration.
Nếu rollback sau khi đã tạo hồ sơ khách không có tài khoản, cần phục hồi bản sao lưu database cùng phiên bản ứng dụng tương ứng; không xóa bảng/khách mới để làm code cũ chạy.

## 7. Quy tắc nghiệp vụ của bản này

```text
Tìm phòng → hồ sơ khách → xem lại → giữ phòng (pending)
→ thu đủ cọc/toàn bộ → confirmed
→ kiểm tra giấy tờ, số khách, phòng sạch → checked_in
→ thêm phát sinh nếu có → thu đủ số dư → checked_out
→ completed; phòng cần được kiểm tra/dọn trước lượt nhận tiếp theo
```

- Preview chưa giữ phòng. Khi bấm Tạo, hệ thống kiểm tra lại và mới giữ phòng.
- Phạm vi là một phòng cho mỗi booking offline, lưu trú tính theo đêm; không phải đặt phòng theo giờ hoặc quản lý đoàn nhiều phòng.
- Ngày nhận từ hôm nay tới 730 ngày; mỗi lượt ở tối đa 365 đêm.
- Nhận phòng đúng ngày đặt. Đến sớm/trễ ngày cần hủy và lập lại lịch phù hợp; bản này chưa hỗ trợ sửa ngày/đổi phòng trực tiếp trên đơn đã xác nhận.
- Giờ nhận sớm/trả trễ trong cùng ngày do lễ tân đối soát và nhập phụ thu theo chính sách khách sạn. Chưa tự tính phụ thu theo giờ.
- Trả phòng sớm không tự giảm tiền phòng đã thỏa thuận.
- Trước ngày nhận phòng: hoàn toàn bộ tiền đã thu. Từ ngày nhận phòng, nếu chưa nhận phòng: giữ lại tối đa tỷ lệ cọc đã lưu trên đơn, hoàn phần còn lại. Không cho hủy đơn đang ở bằng nút hủy; xử lý trả phòng và quyết toán.
- No-show được nhân viên ghi nhận qua nút Hủy/no-show với lý do; chưa tự động hủy booking đã cọc để tránh giải phóng phòng khi khách đang đến trễ.
- Chuyển khoản tại quầy là ghi nhận tiền đã về tài khoản, không phải tạo link thanh toán PayOS. Nhân viên chịu trách nhiệm đối chiếu giao dịch thực tế.
- Nút xác nhận hoàn tiền ghi sổ số tiền đã chi; không tự gọi ngân hàng để chuyển tiền.
- Xác minh giấy tờ là xác nhận nghiệp vụ và lưu người thực hiện; chưa phải module lưu giấy tờ, khai báo lưu trú hoặc hóa đơn điện tử.
- Hồ sơ khách tại quầy chưa có tài khoản web. Việc liên kết với tài khoản đăng ký sau này cần một quy trình xác minh riêng, không tự ghép tài khoản chỉ vì trùng email/điện thoại.
- Chính sách trên cần được khách sạn chấp thuận và thông báo cho khách; đây không phải quy tắc chung bắt buộc cho mọi khách sạn.

## 8. Kiểm tra tích hợp với SQL Server

`mvn package` kiểm tra biên dịch và đóng gói; bộ kiểm tra database được chạy riêng, không tự ghi dữ liệu khi build.

1. Tạo database trống có tên bắt đầu bằng `HotelOfflineTest_`, ví dụ `HotelOfflineTest_Local`.
2. Trong bản sao `script_fixed.sql`, thay mọi `HotelDB` thành tên database kiểm thử rồi chạy bản sao đó.
3. Chọn database kiểm thử, chạy `sql/UC09_StableWorkflow.sql`.
4. Trong cùng terminal, đặt `DB_URL`, `DB_USER`, `DB_PASSWORD` trỏ tới database kiểm thử.
5. Chạy:

```text
python tools/test_offline_workflow.py
```

Cần Python 3, JDK, Maven trên PATH. Test từ chối chạy nếu tên database không bắt đầu bằng `HotelOfflineTest_`.
Test tạo dữ liệu giả trong database được chỉ định; không dùng database live hoặc bản sao đang dùng cho vận hành.

Xem `OFFLINE_BOOKING_VALIDATION.md` để biết kết quả kiểm tra và giới hạn xác nhận.
