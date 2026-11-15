package util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

public final class LanguageSupport {

    public static final String SESSION_KEY = "uiLanguage";
    public static final String COOKIE_NAME = "hotel_language";
    private static final String TOKEN_KEY = "uc24Csrf";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger LOG = Logger.getLogger(LanguageSupport.class.getName());
    private static final String[][] EXTRA = {
        {"en_US", "vi_VN"},
        {"Hotel dashboard", "Tổng quan khách sạn"},
        {"Booking report", "Báo cáo đặt phòng"},
        {"Payment report", "Báo cáo thanh toán"},
        {"Review report", "Báo cáo đánh giá"},
        {"Revenue report", "Báo cáo doanh thu"},
        {"REPORTS", "BÁO CÁO"},
        {"Dashboard", "Tổng quan"},
        {"Bookings", "Đặt phòng"},
        {"Payments", "Thanh toán"},
        {"Reviews", "Đánh giá"},
        {"Revenue", "Doanh thu"},
        {"From date", "Từ ngày"},
        {"To date", "Đến ngày"},
        {"Status", "Trạng thái"},
        {"All", "Tất cả"},
        {"Method", "Phương thức"},
        {"Booking ID", "Mã đặt phòng"},
        {"Rows", "Số dòng"},
        {"Apply", "Áp dụng"},
        {"Reset", "Hiện tất cả"},
        {"Period: ", "Theo kỳ: "},
        {"All periods", "Toàn bộ thời gian"},
        {"Reconcile original payment dates and stored records.", "Đối soát theo ngày thanh toán gốc và dữ liệu đã lưu."},
        {"View period", "Xem theo kỳ"},
        {"Months of year", "Theo tháng của năm"},
        {"View months", "Xem theo tháng"},
        {"Compare years", "So sánh các năm"},
        {"All years. Detail tabs open all periods.", "Toàn bộ các năm. Tab chi tiết mở toàn thời gian."},
        {"Year: ", "Theo năm: "},
        {"Current month", "Tháng hiện tại"},
        {"Retry", "Thử lại"},
        {"Gross collected", "Tổng đã thu"},
        {"Refunded", "Đã hoàn"},
        {"Net after refunds", "Còn lại sau hoàn"},
        {"Refunds are current cumulative refunds of payments whose original dates match the filter, not refunds issued during the period. Wallet withdrawals are not deducted again. Pending/failed transactions do not contribute to gross collected.", "Tiền hoàn là số lũy kế hiện tại của các giao dịch có ngày thanh toán gốc thuộc bộ lọc; không phải tiền hoàn phát sinh trong kỳ. Không trừ thêm tiền rút ví. Giao dịch chờ/thất bại không tính vào tổng đã thu."},
        {"Bookings created in period", "Đơn tạo trong kỳ"},
        {"Current customer profiles", "Hồ sơ khách hiện tại"},
        {"Estimated occupancy", "Công suất ước tính"},
        {"room nights; through", "đêm phòng; đến"},
        {"Occupancy uses elapsed scheduled stays and current inventory, not a complete actual occupancy history. Customers, room statuses and overall reviews are current/all-time values.", "Công suất dựa trên lịch lưu trú đã qua và số phòng hiện có; không phải lịch sử công suất thực tế đầy đủ. Khách, trạng thái phòng và đánh giá tổng hợp là số hiện tại/toàn hệ thống."},
        {"Booking statuses", "Đặt phòng theo trạng thái"},
        {"Current room statuses", "Trạng thái phòng hiện tại"},
        {"Transactions by stored status", "Giao dịch theo trạng thái lưu trong DB"},
        {"Count", "Số giao dịch"},
        {"Original transaction amount", "Tiền giao dịch trước hoàn"},
        {"Status totals use original amounts and payment dates; undated transactions are excluded from the period. These are not net-after-refund totals.", "Bảng trạng thái dùng số tiền gốc và ngày thanh toán; giao dịch chưa có ngày không nằm trong kỳ. Đây không phải tổng còn lại sau hoàn."},
        {"All-time approved reviews", "Đánh giá đã duyệt toàn hệ thống"},
        {"Net after refunds by original payment date", "Còn lại sau hoàn theo ngày thanh toán gốc"},
        {"Details", "Chi tiết"},
        {"Period", "Kỳ"},
        {"Comparison", "So sánh"},
        {"No successful payments.", "Không có giao dịch đã thanh toán."},
        {"Totals cover all filtered results, independent of pagination.", "Thống kê toàn bộ kết quả theo bộ lọc, không đổi khi chuyển trang."},
        {"Total reviews", "Tổng đánh giá"},
        {"Pending / Rejected", "Chờ / Từ chối"},
        {"Approved", "Đã duyệt"},
        {"Approved average", "Trung bình đã duyệt"},
        {"Approved rating distribution", "Phân bố sao đã duyệt"},
        {"No approved reviews.", "Chưa có đánh giá đã duyệt."},
        {"Other ratings (legacy data)", "Mức sao khác (dữ liệu cũ)"},
        {"Detailed records", "Danh sách chi tiết"},
        {"results", "kết quả"},
        {"Booking / Guest", "Đặt phòng / Khách"},
        {"Rooms", "Phòng"},
        {"Payment date", "Ngày thanh toán"},
        {"Amount", "Số tiền"},
        {"Net", "Còn lại"},
        {"Stored discount", "Giảm đã lưu"},
        {"Reference", "Mã giao dịch"},
        {"Submitted", "Ngày gửi"},
        {"Rating", "Sao"},
        {"Comment", "Nhận xét"},
        {"Guest", "Khách lưu trú"},
        {"Created", "Ngày tạo"},
        {"Rooms / Stay", "Phòng / Lưu trú"},
        {"Booking total", "Tổng đơn"},
        {"Notes / Cancellation", "Ghi chú / Hủy"},
        {"Partially refunded", "Hoàn một phần"},
        {"Fully refunded", "Đã hoàn toàn bộ"},
        {"guests", "khách"},
        {"No matching records.", "Không có kết quả phù hợp."},
        {"Language", "Ngôn ngữ"},
        {"← Back to booking history", "← Về lịch sử đặt phòng"},
        {"AVAILABLE BALANCE", "SỐ DƯ KHẢ DỤNG"},
        {"Available Balance", "Số dư khả dụng"},
        {"Request Withdrawal", "Yêu cầu rút tiền"},
        {"My Withdrawal Requests", "Yêu cầu rút tiền của tôi"},
        {"Transaction Type", "Loại giao dịch"},
        {"All Transactions", "Tất cả giao dịch"},
        {"Refund", "Hoàn tiền"},
        {"Withdrawal", "Rút tiền"},
        {"From Date", "Từ ngày"},
        {"To Date", "Đến ngày"},
        {"Apply Filters", "Áp dụng bộ lọc"},
        {"Clear", "Xóa bộ lọc"},
        {"Transaction ID", "Mã giao dịch"},
        {"Date", "Ngày"},
        {"Booking / Description", "Đặt phòng / Nội dung"},
        {"Type", "Loại"},
        {"Booking #", "Đặt phòng #"},
        {"Wallet Transaction", "Giao dịch ví"},
        {"No withdrawal transactions found.", "Chưa có giao dịch rút tiền."},
        {"Completed withdrawals will appear here after payout confirmation.", "Giao dịch rút tiền sẽ xuất hiện sau khi xác nhận đã chi trả."},
        {"No refund transactions found.", "Chưa có giao dịch hoàn tiền."},
        {"No refunds match your selected filters.", "Không có khoản hoàn tiền phù hợp bộ lọc."},
        {"No transactions found.", "Chưa có giao dịch."},
        {"Try adjusting your filters or clearing the selected dates.", "Hãy thay đổi bộ lọc hoặc xóa khoảng ngày đã chọn."},
        {"Previous", "Trước"},
        {"Next", "Tiếp"},
        {"Page", "Trang"},
        {"of", "trên"},
        {"transactions )", "giao dịch )"},
        {"Request Withdrawal - Luxe Escape", "Yêu cầu rút tiền - Luxe Escape"},
        {"Submit a request to withdraw money from your wallet.", "Gửi yêu cầu rút tiền từ ví của bạn."},
        {"Your withdrawal request was submitted. It is now pending review.", "Đã gửi yêu cầu rút tiền. Yêu cầu đang chờ duyệt."},
        {"Please enter a valid withdrawal amount.", "Vui lòng nhập số tiền rút hợp lệ."},
        {"Please check your bank information.", "Vui lòng kiểm tra thông tin ngân hàng."},
        {"Insufficient available balance.", "Số dư khả dụng không đủ."},
        {"Unable to submit your request.", "Chưa gửi được yêu cầu."},
        {"Wallet Balance:", "Số dư ví:"},
        {"Reserved for Withdrawals:", "Đang giữ cho yêu cầu rút tiền:"},
        {"Withdrawal Information", "Thông tin rút tiền"},
        {"Withdrawal Amount (VND)", "Số tiền rút (VND)"},
        {"Amount cannot exceed your available balance.", "Số tiền không được vượt quá số dư khả dụng."},
        {"Bank Name", "Tên ngân hàng"},
        {"Account Number", "Số tài khoản"},
        {"Account Holder", "Chủ tài khoản"},
        {"Withdrawal requests must be reviewed before payment is confirmed. Check your bank details carefully.", "Yêu cầu rút tiền cần được duyệt trước khi xác nhận chi trả. Hãy kiểm tra kỹ thông tin ngân hàng."},
        {"Submit Withdrawal Request", "Gửi yêu cầu rút tiền"},
        {"Back to Wallet", "Về ví"},
        {"My Requests", "Yêu cầu của tôi"},
        {"My Withdrawal Requests - Luxe Escape", "Yêu cầu rút tiền của tôi - Luxe Escape"},
        {"Track your withdrawal requests and their current status.", "Theo dõi yêu cầu rút tiền và trạng thái hiện tại."},
        {"New Withdrawal Request", "Tạo yêu cầu rút tiền"},
        {"Withdrawal History", "Lịch sử rút tiền"},
        {"Request ID", "Mã yêu cầu"},
        {"Bank", "Ngân hàng"},
        {"Pending", "Chờ xử lý"},
        {"Paid", "Đã thanh toán"},
        {"Rejected", "Đã từ chối"},
        {"Reason:", "Lý do:"},
        {"Awaiting payout confirmation", "Đang chờ xác nhận chi trả"},
        {"Payout recorded", "Đã ghi nhận chi trả"},
        {"No withdrawal requests found.", "Chưa có yêu cầu rút tiền."},
        {"Withdrawal Management - Luxe Escape", "Quản lý rút tiền - Luxe Escape"},
        {"Withdrawal Management", "Quản lý rút tiền"},
        {"Review and manage customer withdrawal requests.", "Duyệt và quản lý yêu cầu rút tiền của khách."},
        {"Withdrawal request updated successfully.", "Đã cập nhật yêu cầu rút tiền."},
        {"Request could not be processed. Check its status and your permissions.", "Chưa xử lý được yêu cầu. Hãy kiểm tra trạng thái và quyền của bạn."},
        {"Filter by Status", "Lọc theo trạng thái"},
        {"All Requests", "Tất cả yêu cầu"},
        {"Apply Filter", "Áp dụng bộ lọc"},
        {"Customer ID", "Mã khách hàng"},
        {"Actions", "Thao tác"},
        {"Approve", "Duyệt"},
        {"Reject", "Từ chối"},
        {"Verified Payout Reference", "Mã giao dịch chi trả đã kiểm tra"},
        {"Record as Paid", "Ghi nhận đã chi trả"},
        {"Payment recorded", "Đã ghi nhận chi trả"},
        {"Reason for rejection", "Lý do từ chối"},
        {"Guest profiles without an account can verify their email to activate one and retain booking history:", "Khách tại quầy chưa có tài khoản có thể xác minh email để kích hoạt và giữ lịch sử đặt phòng:"},
        {"Account activation link", "Liên kết kích hoạt gửi cho khách"},
        {". Guests must open the link on their own device while signed out. Do not create an account on their behalf.", ". Khách mở liên kết trên thiết bị của mình, chưa đăng nhập. Không tự tạo tài khoản thay khách."},
        {"Dates or guest count changed. Search for rooms again to continue.", "Ngày hoặc số khách đã đổi. Tìm lại phòng để tiếp tục."},
        {"Quote details", "Chi tiết báo giá"},
        {"Price checked", "Đã kiểm tra giá"},
        {"Rate / night", "Đơn giá / đêm"},
        {"Estimated discount", "Giảm dự kiến"},
        {"Estimated total", "Tổng dự kiến"},
        {"Estimated deposit", "Cọc dự kiến"},
        {"Details changed or the quote expired. Check the quote again before creating the booking.", "Thông tin đã thay đổi hoặc báo giá hết hạn. Xem và kiểm tra giá lại trước khi tạo."},
        {"Refresh quote", "Cập nhật báo giá"},
        {"Check system quote", "Xem và kiểm tra giá"},
        {"Confirm booking creation", "Xác nhận tạo đặt phòng"},
        {"Viewing a quote does not hold a room. Confirmation is valid for 5 minutes and is checked again during creation. Promotions are checked when collecting payment. Creating a booking does not record payment.", "Xem giá chưa giữ phòng. Báo giá có hiệu lực xác nhận trong 5 phút; hệ thống kiểm tra lại khi tạo. Khuyến mãi vẫn được kiểm tra ở bước thu tiền. Tạo đơn chưa ghi nhận thanh toán."},
        {"The previous request outcome is unclear. Check booking history or retry the same request.", "Chưa rõ yêu cầu trước đã tạo đơn hay chưa. Kiểm tra lịch sử trước, hoặc kiểm tra lại đúng yêu cầu cũ."},
        {"Check previous request", "Kiểm tra lại yêu cầu cũ"},
        {"Period: {value}", "Kỳ: {value}"},
        {"Year: {value}", "Theo năm: {value}"},
        {"{count} results", "{count} kết quả"},
        {"{count} guests", "{count} khách"},
        {"Room {number}", "Phòng {number}"},
        {"Select a room", "Chọn phòng"},
        {"Booking information", "Thông tin đặt phòng"},
        {"{count} nights", "{count} đêm"},
        {"Room charge ({count} nights)", "Tiền phòng ({count} đêm)"},
        {"Estimated deposit ({value})", "Cọc dự kiến ({value})"},
        {"Pending / Rejected: {value}", "Chờ / Từ chối: {value}"},
        {"{value}% of total", "{value}% tổng tiền"},
        {"Wallet Balance: {value}", "Số dư ví: {value}"},
        {"Reserved for Withdrawals: {value}", "Đang giữ cho yêu cầu rút tiền: {value}"},
        {"Reason: {value}", "Lý do: {value}"},
        {"Page {page} of {pages} ({count} transactions)", "Trang {page} trên {pages} ({count} giao dịch)"},
        {"{count} night", "{count} đêm"},
        {"{count} guest", "{count} khách"},
        {"Completed", "Đã hoàn tất"},
        {"Cancelled", "Đã hủy"},
        {"Checked in", "Đã nhận phòng"},
        {"Checked out", "Đã trả phòng"},
        {"Confirmed", "Đã xác nhận"},
        {"Failed", "Thất bại"},
        {"Active", "Đang hoạt động"},
        {"Locked", "Đã khóa"},
        {"Inactive", "Ngừng hoạt động"},
        {"Hotel Wallet - Luxe Escape", "Ví khách sạn - Luxe Escape"},
        {"Hotel Wallet", "Ví khách sạn"},
        {"Financial overview and customer payment records.", "Tổng quan tài chính và các giao dịch thanh toán của khách."},
        {"Total Recorded Payments", "Tổng tiền thanh toán đã ghi nhận"},
        {"Total Refunds", "Tổng tiền hoàn"},
        {"Active Booking Payments", "Tiền đã thu của đơn đang chờ hoặc đã xác nhận"},
        {"Customer Wallet Balances", "Tổng số dư ví khách"},
        {"Recent Payment Transactions", "Giao dịch thanh toán gần đây"},
        {"Payment ID", "Mã thanh toán"},
        {"Payment Status", "Trạng thái thanh toán"},
        {"Booking Status", "Trạng thái đặt phòng"},
        {"Payment Date", "Ngày thanh toán"},
        {"No payment records found.", "Chưa có giao dịch thanh toán."},
        {"Customer Wallet Management", "Quản lý ví khách hàng"},
        {"View customer wallet balances and refund transaction history.", "Xem số dư ví khách hàng và lịch sử hoàn tiền."},
        {"View Customer Wallets", "Xem ví khách hàng"},
        {"Wallet Management - Luxe Escape", "Quản lý ví - Luxe Escape"},
        {"Wallet Management", "Quản lý ví"},
        {"View customer balances and refund transactions.", "Xem số dư khách hàng và giao dịch hoàn tiền."},
        {"Customer Wallets", "Ví khách hàng"},
        {"Search by Customer ID", "Tìm theo mã khách hàng"},
        {"Enter Customer ID", "Nhập mã khách hàng"},
        {"Balance", "Số dư"},
        {"View Transactions", "Xem giao dịch"},
        {"Transactions - Customer #", "Giao dịch - Khách #"},
        {"Invalid activation information.", "Thông tin kích hoạt không hợp lệ."},
        {"Username/email is already in use. If you just activated an account, try signing in before retrying.", "Username/email đã được dùng. Nếu vừa kích hoạt, thử đăng nhập trước khi thực hiện lại."},
        {"The profile changed or already has an account. Check sign-in or request a new code.", "Hồ sơ đã thay đổi hoặc đã có tài khoản. Kiểm tra đăng nhập hoặc yêu cầu mã mới."},
        {"The guest profile is incomplete. Contact reception to update it, then verify the email again.", "Hồ sơ khách chưa đủ thông tin. Liên hệ lễ tân cập nhật rồi xác minh email lại."},
        {"Enter a valid email address of up to 100 characters.", "Email không hợp lệ hoặc vượt 100 ký tự."},
        {"Too many requests. Try again after 15 minutes.", "Đã gửi quá nhiều yêu cầu. Thử lại sau 15 phút."},
        {"Unable to check the profile. Please try again later.", "Chưa kiểm tra được hồ sơ. Thử lại sau."},
        {"Unable to send email. Please try again later.", "Chưa gửi được email. Vui lòng thử lại sau."},
        {"The code expired or no attempts remain. Request a new code.", "Mã đã hết hạn hoặc hết lượt thử. Yêu cầu mã mới."},
        {"Too many verification requests. Please try again later.", "Quá nhiều yêu cầu xác minh. Thử lại sau."},
        {"The code is incorrect, expired, or the profile is not eligible for activation.", "Mã không đúng, hết hạn hoặc hồ sơ chưa đủ điều kiện kích hoạt."},
        {"Username must have 3–50 characters. Password must have 8–128 characters with uppercase, lowercase, numbers and special characters, no whitespace, and matching confirmation.", "Username cần 3–50 ký tự. Mật khẩu cần 8–128 ký tự, đủ chữ hoa/thường, số, ký tự đặc biệt, không có khoảng trắng; xác nhận phải khớp."},
        {"The outcome could not be confirmed. Try signing in first. If that fails, request a new code and retry.", "Chưa xác nhận được kết quả. Thử đăng nhập trước; nếu chưa được, yêu cầu mã mới rồi thử lại."},
        {"Password changed. The sign-in credentials have been sent by email.", "Đã đổi mật khẩu và gửi email thông tin đăng nhập."},
        {"Account created. The sign-in credentials have been sent by email.", "Đã tạo tài khoản và gửi email thông tin đăng nhập."},
        {"The password has changed and the old password no longer works, but email delivery could not be confirmed. Check the inbox and mail settings before requesting another password.", "Mật khẩu đã được đổi trong hệ thống, mật khẩu cũ không còn hiệu lực, nhưng chưa xác nhận gửi được email. Kiểm tra hộp thư và cấu hình gửi thư trước khi yêu cầu mật khẩu mới lần nữa."},
        {"Account created, but email delivery could not be confirmed. Check the inbox and mail settings, then use Inspect to send a new password. Do not create the account again.", "Đã tạo tài khoản nhưng chưa xác nhận gửi được email. Kiểm tra hộp thư/cấu hình gửi thư, sau đó dùng Inspect để gửi mật khẩu mới. Không tạo lại tài khoản."},
        {"Mail settings are invalid. The password has not changed. Check the settings and try again.", "Cấu hình gửi thư chưa hợp lệ. Mật khẩu chưa thay đổi; kiểm tra cấu hình rồi thử lại."},
        {"The save result could not be confirmed. Reload the list and inspect the account before retrying.", "Chưa xác nhận được kết quả lưu. Tải lại danh sách và kiểm tra tài khoản trước khi thử lại."},
        {"This email/identifier belongs to another guest profile. Find the existing profile; walk-in guests should verify their email through account activation instead of creating a duplicate account.", "Email/định danh đã thuộc hồ sơ khách khác. Tìm hồ sơ có sẵn; khách tại quầy cần xác minh qua liên kết kích hoạt, không tạo tài khoản trùng."},
        {"Username must contain 3–50 characters, without @ or control characters.", "Username: 3–50 ký tự, không chứa @ hoặc ký tự điều khiển."},
        {"Phone must contain 7–15 digits, optionally starting with +.", "Số điện thoại cần 7–15 chữ số, có thể bắt đầu bằng +."},
        {"First and last names are required. Each allows up to 50 characters; the full name allows up to 100.", "Họ và tên bắt buộc, mỗi phần tối đa 50 ký tự, tổng tối đa 100."},
        {"Please select a valid role.", "Role không hợp lệ."},
        {"Please select a valid account status.", "Status không hợp lệ."},
        {"Address fields exceed the allowed length or contain unsupported control characters.", "Địa chỉ vượt giới hạn hoặc có ký tự điều khiển."},
        {"Your administrator access has changed. Please sign in again.", "Quyền Administrator đã thay đổi. Hãy đăng nhập lại."},
        {"This account was not found or is not available in User Management.", "Tài khoản không tồn tại hoặc ngoài phạm vi UC18."},
        {"This account has changed. Close the form, reload the page and try again.", "Dữ liệu đã thay đổi. Đóng form, tải lại và thử lại."},
        {"This username or email address is already assigned to another account.", "Username hoặc Email đã được gán cho tài khoản khác."},
        {"Please review the account information and try again.", "Thông tin tài khoản không hợp lệ."},
        {"You cannot lock, deactivate or demote your own signed-in account.", "Không thể tự khóa, vô hiệu hóa hoặc hạ quyền tài khoản đang đăng nhập."},
        {"You cannot lock your own signed-in account.", "Không thể tự khóa tài khoản đang đăng nhập."},
        {"This account is inactive. Use Edit to change its status.", "Tài khoản Inactive: dùng Edit để chọn trạng thái rõ ràng."},
        {"Use Change Password to update your own password.", "Dùng Change Password cho tài khoản của bạn."},
        {"Add an email address to this account first.", "Cần cập nhật Email trước."},
        {"You cannot delete your own signed-in account.", "Không thể tự xóa tài khoản đang đăng nhập."},
        {"This account has transaction history and cannot be deleted. Choose Locked or Inactive instead.", "Tài khoản có lịch sử giao dịch. Hãy chọn Locked hoặc Inactive."},
        {"Account saved. The sign-in credentials have been sent by email.", "Đã lưu tài khoản và gửi email thông tin đăng nhập."},
        {"Account saved, but the email could not be sent. Check email delivery settings, then use Inspect > Send new password. Do not create the account again.", "Đã lưu tài khoản nhưng chưa gửi được email. Kiểm tra SMTP/APP_BASE_URL, sau đó dùng nút gửi mật khẩu mới trong Inspect. Không tạo lại tài khoản."},
        {"Changes saved successfully.", "Đã lưu thay đổi."},
        {"The unused account has been deleted successfully.", "Đã xóa tài khoản chưa sử dụng."},
        {"This account has related records and cannot be deleted.", "Tài khoản còn dữ liệu liên quan nên không thể xóa."},
        {"Another update is in progress. Reload the page and try again.", "Có cập nhật đồng thời. Tải lại trang và thử lại."},
        {"Your changes could not be saved. Please try again.", "Không thể lưu thay đổi. Database đã rollback; hãy thử lại."},
        {"Unable to load user accounts. Please try again.", "Không thể tải danh sách tài khoản. Hãy thử lại."}
    };
    private static final String[][] ACTIVATION = {
        {"title", "Walk-in account activation | Luxe Escape", "Kích hoạt tài khoản khách tại quầy | Luxe Escape"},
        {"heading", "Activate account", "Kích hoạt tài khoản"},
        {"success", "Account created and linked to the walk-in profile. Booking history is preserved.", "Đã tạo tài khoản và liên kết hồ sơ tại quầy. Lịch sử đặt phòng được giữ nguyên."},
        {"intro", "For walk-in guests without a website account. Verify the email on your profile to retain booking history.", "Dành cho khách đã đặt tại quầy nhưng chưa có tài khoản website. Xác minh email trên hồ sơ để giữ lại lịch sử đặt phòng."},
        {"email", "Email provided at the front desk", "Email đã cung cấp tại quầy"},
        {"send", "Send / resend verification code", "Gửi / gửi lại mã xác minh"},
        {"sent", "If the profile is eligible, a code has been emailed. It expires in 5 minutes and allows up to 3 activation submissions. Contact reception if the profile email is missing or incorrect.", "Nếu hồ sơ đủ điều kiện, mã đã được gửi tới email. Mã có hiệu lực 5 phút, tối đa 3 lần gửi form kích hoạt. Không có email trên hồ sơ hoặc email sai: liên hệ lễ tân."},
        {"verifying", "Email being verified:", "Email đang xác minh:"},
        {"attempts", "Attempts remaining:", "Lượt còn lại:"},
        {"otp", "6-digit verification code", "Mã xác minh 6 số"},
        {"username", "New username", "Tên đăng nhập mới"},
        {"password", "New password", "Mật khẩu mới"},
        {"confirm", "Confirm password", "Xác nhận mật khẩu"},
        {"passwordHint", "Password: 8–128 characters with uppercase, lowercase, numbers and special characters; no whitespace.", "Mật khẩu 8–128 ký tự: có chữ hoa, chữ thường, số và ký tự đặc biệt; không có khoảng trắng."},
        {"activate", "Verify and create account", "Xác minh và tạo tài khoản"},
        {"existing", "Already have an account?", "Đã có tài khoản?"},
        {"login", "Sign in", "Đăng nhập"},
        {"or", "or", "hoặc"},
        {"recover", "recover your password", "khôi phục mật khẩu"}
    };
    private static final Map<String, String> RAW_EN = load("messages_en.properties");
    private static final Map<String, String> EN = complete(RAW_EN, "en");
    private static final Map<String, String> VI = complete(vietnamese(), "vi");
    private static final Map<String, String> LOOKUP = buildLookup();

    private LanguageSupport() {
    }

    public static boolean supported(String value) {
        return "en".equals(value) || "vi".equals(value);
    }

    private static Map<String, String> load(String name) {
        Properties p = new Properties();
        try (InputStream input = LanguageSupport.class.getResourceAsStream("/i18n/" + name)) {
            if (input == null) {
                throw new IllegalStateException("Missing i18n/" + name);
            }
            StringBuilder clean = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String t = line.trim();
                    if (t.startsWith("<<<<<<<") || t.startsWith("=======") || t.startsWith(">>>>>>>")) {
                        LOG.warning("Ignored merge marker in " + name);
                        continue;
                    }
                    clean.append(line).append('\n');
                }
            }
            p.load(new StringReader(clean.toString()));
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load " + name, ex);
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : new TreeSet<>(p.stringPropertyNames())) {
            if (!key.isBlank()) {
                result.put(key, p.getProperty(key));
            }
        }
        return result;
    }

    private static Map<String, String> vietnamese() {
        Map<String, String> result = new LinkedHashMap<>(RAW_EN);
        try {
            result.putAll(load("messages_vi.properties"));
        } catch (IllegalStateException ex) {
            LOG.warning("Vietnamese resource unavailable; using English fallback.");
        }
        return result;
    }

    private static Map<String, String> complete(Map<String, String> source, String lang) {
        Map<String, String> result = new LinkedHashMap<>(source);
        result.put("language.choose", "vi".equals(lang) ? "Ngôn ngữ" : "Language");
        result.put("booking.list.stay.from", "vi".equals(lang) ? "Lưu trú từ ngày" : "Stay from");
        result.put("booking.list.stay.to", "vi".equals(lang) ? "Lưu trú đến hết ngày" : "Stay through");
        for (int i = 0; i < EXTRA.length; i++) {
            result.put("uc24.extra." + i, EXTRA[i]["vi".equals(lang) ? 1 : 0]);
        }
        for (String[] row : ACTIVATION) {
            result.put("uc24.activation." + row[0], row["vi".equals(lang) ? 2 : 1]);
        }
        return Collections.unmodifiableMap(result);
    }

    private static String normalized(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> buildLookup() {
        Map<String, String> result = new LinkedHashMap<>();
        Set<String> ambiguous = new HashSet<>();
        for (String key : EN.keySet()) {
            for (Map<String, String> bundle : List.of(EN, VI)) {
                String value = bundle.get(key);
                if (value == null || value.isBlank()) {
                    continue;
                }
                String n = normalized(value), old = result.get(n);
                if (old == null) {
                    result.put(n, key);
                } else if (!Objects.equals(EN.get(old), EN.get(key)) || !Objects.equals(VI.get(old), VI.get(key))) {
                    ambiguous.add(n);
                }
            }
        }
        ambiguous.forEach(result::remove);
        // These aliases are applied only to explicitly selected UI labels.
        for (int i = 0; i < EXTRA.length; i++) {
            String key = "uc24.extra." + i;
            result.put(normalized(EXTRA[i][0]), key);
            result.put(normalized(EXTRA[i][1]), key);
        }
        result.put("checked_in", "offline.state.checked_in");
        result.put("checked_out", "offline.state.checked_out");
        result.put("bank_transfer", "offline.bank");
        result.put("cash", "offline.cash");
        return Collections.unmodifiableMap(result);
    }

    public static Map<String, String> messages(String lang) {
        return "vi".equals(lang) ? VI : EN;
    }

    public static Map<String, String> lookup() {
        return LOOKUP;
    }

    public static String resolve(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null)try {
            Object value = session.getAttribute(SESSION_KEY);
            if (value instanceof String && supported((String) value)) {
                return (String) value;
            }
        } catch (IllegalStateException ignored) {
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName()) && supported(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return "en";
    }

    public static String token(HttpSession session) {
        synchronized (session) {
            Object saved = session.getAttribute(TOKEN_KEY);
            if (saved instanceof String) {
                return (String) saved;
            }
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(TOKEN_KEY, value);
            return value;
        }
    }

    public static boolean validToken(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            Object saved = session == null ? null : session.getAttribute(TOKEN_KEY);
            String supplied = request.getParameter("csrfToken");
            return saved instanceof String && supplied != null && supplied.length() <= 100
                    && MessageDigest.isEqual(((String) saved).getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    public static void save(HttpServletRequest request, HttpServletResponse response, String language) {
        if (!supported(language)) {
            throw new IllegalArgumentException("Unsupported language");
        }
        request.getSession().setAttribute(SESSION_KEY, language);
        Cookie cookie = new Cookie(COOKIE_NAME, language);
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setMaxAge(365 * 24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    public static String json(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 32 || c == '<' || c == '>' || c == '&' || c == '\u2028' || c == '\u2029') {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.append('"').toString();
    }
}
