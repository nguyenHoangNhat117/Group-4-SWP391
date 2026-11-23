package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import util.AuthSupport;

public final class OfflineBookingDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    public final String requestKey = UUID.randomUUID().toString();

    public final LocalDate checkIn;
    public final LocalDate checkOut;

    public final int guests;
    public final int adults;
    public final int children;
    public final int quantity;
    public final Map<Integer, Integer> rooms;

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
            int adults,
            int children,
            Map<Integer, Integer> rooms,
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
        this.adults = adults;
        this.children = children;
        this.guests = countGuests(adults, children);
        this.rooms = Collections.unmodifiableMap(new TreeMap<>(rooms));
        this.quantity = roomCount(this.rooms, adults);
        this.customerId = customerId;

        this.firstName = AuthSupport.text(firstName);
        this.lastName = AuthSupport.text(lastName);
        this.email = AuthSupport.text(email);
        this.phone = util.OfflineCustomerContact.phone(phone);
        this.promotionCode = AuthSupport.text(promotionCode);
        this.specialRequest = AuthSupport.text(specialRequest);

        validate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    public void validate(LocalDate today) {
        if (countGuests(adults, children) != guests || rooms == null || roomCount(rooms, adults) != quantity) {
            throw new IllegalArgumentException("Thông tin khách hoặc phòng đã thay đổi. Vui lòng xem lại báo giá.");
        }
        validateStay(checkIn, checkOut, guests, today);

        if (customerId != null && customerId <= 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn loại phòng, số lượng và khách hàng hợp lệ."
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
                || (!email.isEmpty() && !util.AuthUcPolicy.validEmail(email))
                || !validPhone(phone))) {
            throw new IllegalArgumentException(
                    "Khách mới cần họ, tên hợp lệ, email hợp lệ (nếu nhập) "
                    + "và điện thoại 7–15 chữ số (có thể bắt đầu bằng +)."
            );
        }
    }

    private static int roomCount(Map<Integer, Integer> rooms, int adults) {
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("Chọn ít nhất một phòng.");
        }
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : rooms.entrySet()) {
            int count = entry.getValue();
            if (entry.getKey() < 1 || count < 1 || count > adults - total) {
                throw new IllegalArgumentException("Mỗi phòng cần ít nhất một người lớn. Kiểm tra lại số phòng.");
            }
            total += count;
        }
        return total;
    }

    public static int countGuests(int adults, int children) {
        if (adults < 1) {
            throw new IllegalArgumentException("Cần ít nhất một người từ đủ 18 tuổi đi cùng và lưu trú.");
        }
        if (adults > 1000 || children < 0 || children > 1000 - adults) {
            throw new IllegalArgumentException("Tổng số người lớn và trẻ em phải từ 1 đến 1000.");
        }
        return adults + children;
    }

    public int getAdults() {
        return adults;
    }

    public int getChildren() {
        return children;
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
                || java.time.temporal.ChronoUnit.DAYS.between(from, to) > 365
                || guests <= 0
                || guests > 1000) {
            throw new IllegalArgumentException(
                    "Ngày nhận từ hôm nay và trong 730 ngày tới; lưu trú 1–365 đêm; số khách từ 1 đến 1000."
            );
        }
    }

    public static boolean validPhone(String value) {
        return util.OfflineCustomerContact.validPhone(value);
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

    public int getQuantity() {
        return quantity;
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
