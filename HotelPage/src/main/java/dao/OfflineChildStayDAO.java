package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

final class OfflineChildStayDAO {

    private OfflineChildStayDAO() {
    }

    static Map<Integer, Map<String, Object>> roomGuests(Connection connection, int bookingId, boolean hasActualCounts)
            throws SQLException {
        boolean legacyCounts = hasLegacyCounts(connection);
        String legacy = legacyCounts
                ? "d.OfflineUnderSixCount,d.OfflineActualUnderSixCount" : "CAST(0 AS int),CAST(NULL AS int)";
        String actual = hasActualCounts ? "d.ActualGuestCount" : "CAST(NULL AS int)";
        String sql = "SELECT d.BookingDetailID,d.GuestCount,t.Capacity," + legacy + "," + actual
                + ",r.RoomNumber"
                + " FROM BookingDetail d "
                + "LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber AND d.IsProvisionalRoom=0 "
                + "LEFT JOIN RoomType t ON t.RoomTypeID=COALESCE(d.RoomTypeID,r.RoomTypeID) "
                + "WHERE d.BookingID=? ORDER BY d.BookingDetailID";
        Map<Integer, Map<String, Object>> rooms = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    Map<String, Object> room = new LinkedHashMap<>();
                    room.put("capacity", rows.getInt(3));
                    int plannedGuests = rows.getInt(2) + rows.getInt(4);
                    room.put("plannedGuests", plannedGuests);
                    room.put("arrivalLimit", Math.min(1000, Math.min(plannedGuests, rows.getInt(3))));
                    room.put("legacyChildren", rows.getInt(4));
                    room.put("legacyActualChildren", rows.getObject(5));
                    room.put("actualGuests", rows.getObject(6));
                    room.put("roomNumber", rows.getObject(7));
                    rooms.put(rows.getInt(1), room);
                }
            }
        }
        return rooms;
    }

    private static boolean hasLegacyCounts(Connection connection) throws SQLException {
        String sql = "SELECT CASE WHEN COL_LENGTH('dbo.BookingDetail','OfflineUnderSixCount') IS NOT NULL "
                + "AND COL_LENGTH('dbo.BookingDetail','OfflineActualUnderSixCount') IS NOT NULL THEN 1 ELSE 0 END";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rows = ps.executeQuery()) {
            return rows.next() && rows.getBoolean(1);
        }
    }

    static boolean hasActualRoomGuestCount(Connection connection) throws SQLException {
        String sql = "SELECT COL_LENGTH('dbo.BookingDetail','ActualGuestCount')";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rows = ps.executeQuery()) {
            return rows.next() && rows.getObject(1) != null;
        }
    }

    static Map<Integer, Integer> arrivalGuestsByRoom(Connection connection, int bookingId,
            Map<String, String[]> form) throws SQLException {
        String sql = "SELECT d.BookingDetailID,t.Capacity FROM BookingDetail d "
                + "LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber "
                + "JOIN RoomType t WITH(HOLDLOCK) ON t.RoomTypeID=COALESCE(d.RoomTypeID,r.RoomTypeID) "
                + "WHERE d.BookingID=?";
        int total = 0;
        Map<Integer, Integer> guestsByRoom = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    int id = rows.getInt(1);
                    int guests = guestCount(form.get("guests-" + id));
                    if (guests > rows.getInt(2)) {
                        throw new IllegalArgumentException("Một phòng vượt sức chứa. Kiểm tra lại số khách từng phòng.");
                    }
                    if (!Arrays.equals(form.get("adultPresent-" + id), new String[]{"yes"})) {
                        throw new IllegalArgumentException("Xác nhận mỗi phòng có người từ đủ 18 tuổi đã đối chiếu giấy tờ.");
                    }
                    total += guests;
                    if (total > 1000) {
                        throw new IllegalArgumentException("Tổng khách thực tế không được vượt 1000.");
                    }
                    guestsByRoom.put(id, guests);
                }
            }
        }
        int rooms = guestsByRoom.size();
        if (rooms == 0 || form.keySet().stream().filter(name -> name.startsWith("guests-")).count() != rooms
                || form.keySet().stream().filter(name -> name.startsWith("adultPresent-")).count() != rooms) {
            throw new IllegalArgumentException("Thông tin khách chưa khớp với các phòng trong đơn. Vui lòng mở lại đơn.");
        }
        return guestsByRoom;
    }

    static void saveArrivalGuests(Connection connection, int bookingId, Map<Integer, Integer> guestsByRoom)
            throws SQLException {
        String sql = "UPDATE BookingDetail SET ActualGuestCount=? WHERE BookingID=? AND BookingDetailID=? "
                + "AND RoomNumber IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(2, bookingId);
            for (Map.Entry<Integer, Integer> room : guestsByRoom.entrySet()) {
                ps.setInt(1, room.getValue());
                ps.setInt(3, room.getKey());
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không lưu được số khách thực tế cho phòng đã gán.");
                }
            }
        }
    }

    static int guestCount(String[] values) {
        if (values != null && values.length == 1) {
            try {
                int count = Integer.parseInt(values[0]);
                if (count >= 1 && count <= 1000) {
                    return count;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalArgumentException("Nhập đủ số khách thực tế cho từng phòng.");
    }
}
