package util;

/** Quy tắc chiếm phòng dùng chung cho tìm kiếm, lịch và xác nhận booking. */
public final class RoomOccupancySql {
    private RoomOccupancySql() { }

    private static String alias(String value) {
        if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid internal SQL alias");
        }
        return value;
    }

    public static String endDate(String bookingAlias, String detailAlias) {
        String b = alias(bookingAlias);
        String d = alias(detailAlias);
        // ActualCheckOutAt được lưu bằng SYSUTCDATETIME(); ngày lưu trú theo Việt Nam.
        String actualDate = "CONVERT(date,DATEADD(MINUTE,420," + b + ".ActualCheckOutAt))";
        return "(CASE WHEN " + b + ".Status IN ('checked_out','completed')"
                + " AND " + b + ".ActualCheckOutAt IS NOT NULL"
                + " AND " + actualDate + ">=" + d + ".CheckInDate"
                + " THEN " + actualDate + " ELSE " + d + ".CheckOutDate END)";
    }

    public static String overlaps(String bookingAlias, String detailAlias) {
        String b = alias(bookingAlias);
        String d = alias(detailAlias);
        String end = endDate(b, d);
        // Hai dấu ? giữ đúng thứ tự cũ: ngày khách mới trả, rồi ngày khách mới nhận.
        // Lượt nhận/trả trong cùng ngày không chiếm thêm đêm sau khi trả và dọn xong.
        return b + ".Status<>'cancelled' AND " + end + ">" + d + ".CheckInDate"
                + " AND " + d + ".CheckInDate<? AND " + end + ">?";
    }
}
