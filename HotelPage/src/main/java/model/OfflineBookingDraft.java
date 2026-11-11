package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import util.AuthSupport;

public final class OfflineBookingDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    public final String requestKey = UUID.randomUUID().toString();

    public final LocalDate checkIn;
    public final LocalDate checkOut;

    public final int guests;
    public final int roomNumber;

    public final Integer customerId;

    public final String firstName;
    public final String lastName;
    public final String email;
    public final String phone;
    public final String promotionCode;
    public final String specialRequest;

    public OfflineBookingDraft(
            LocalDate checkIn,
            LocalDate checkOut,
            int guests,
            int roomNumber,
            Integer customerId,
            String firstName,
            String lastName,
            String email,
            String phone,
            String promotionCode,
            String specialRequest
    ) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.roomNumber = roomNumber;
        this.customerId = customerId;

        this.firstName = AuthSupport.text(firstName);
        this.lastName = AuthSupport.text(lastName);
        this.email = AuthSupport.text(email);
        this.phone = AuthSupport.text(phone).replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        this.promotionCode = AuthSupport.text(promotionCode);
        this.specialRequest = AuthSupport.text(specialRequest);

        validate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    public void validate(LocalDate today) {
        validateStay(checkIn, checkOut, guests, today);

        if (roomNumber <= 0
                || (customerId != null && customerId <= 0)) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phòng và khách hàng hợp lệ."
            );
        }

        if (promotionCode.length() > 50
                || specialRequest.length() > 500) {
            throw new IllegalArgumentException(
                    "Mã khuyến mãi tối đa 50 ký tự; "
                    + "yêu cầu đặc biệt tối đa 500 ký tự."
            );
        }

        if (customerId == null
                && (!validName(firstName)
                || !validName(lastName)
                || (lastName + " " + firstName).length() > 100
                || (!email.isEmpty() && !AuthSupport.validEmail(email))
                || !validPhone(phone))) {
            throw new IllegalArgumentException(
                    "Khách mới cần họ, tên hợp lệ, email hợp lệ (nếu nhập) "
                    + "và điện thoại 7–15 chữ số (có thể bắt đầu bằng +)."
            );
        }
    }

    public static void validateStay(
            LocalDate from,
            LocalDate to,
            int guests,
            LocalDate today
    ) {
        if (from == null
                || to == null
                || from.isBefore(today)
                || !to.isAfter(from)
                || to.isAfter(today.plusDays(730))
                || java.time.temporal.ChronoUnit.DAYS.between(from,to)>365
                || guests <= 0
                || guests > 1000) {
            throw new IllegalArgumentException(
                    "Ngày nhận từ hôm nay và trong 730 ngày tới; lưu trú 1–365 đêm; số khách từ 1 đến 1000."
            );
        }
    }

    public static boolean validPhone(String value) {
        return value != null && value.matches("[+]?[0-9]{7,15}");
    }

    private static boolean validName(String value) {
        return !value.isBlank()
                && value.length() <= 50
                && value.chars().noneMatch(Character::isISOControl);
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getGuests() {
        return guests;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }
}
