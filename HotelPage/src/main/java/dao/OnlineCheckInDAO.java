package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import util.ReportPeriod;
import util.RoomOccupancySql;

/**
 * Dữ liệu chọn phòng và kiểm tra phòng dùng chung tại thời điểm nhận phòng.
 */
public final class OnlineCheckInDAO {

    private static final String NO_ROOM = "Không đủ phòng sẵn sàng đúng loại. Kiểm tra phòng bẩn, bảo trì hoặc khách chưa trả rồi thử lại.";
    private static final String ROOM_CHANGED = "Phòng đã chọn vừa thay đổi hoặc có lịch trùng. Vui lòng tải lại và chọn lại.";
    private static final String ELIGIBLE_ROOM = "r.RoomTypeID=? AND r.Status='available' AND r.NeedsCleaning=0 "
            + "AND NOT EXISTS (SELECT 1 FROM BookingDetail d JOIN Booking b ON b.BookingID=d.BookingID "
            + "WHERE d.RoomNumber=r.RoomNumber AND b.BookingID<>? AND (b.Status='checked_in' OR ("
            + RoomOccupancySql.overlaps("b", "d") + ")))";

    private OnlineCheckInDAO() {
    }

    public static final class Slot {

        private final int id;
        private final int typeId;
        private final String typeName;
        private final Integer assigned;
        private final LocalDate checkOut;
        private final int capacity;
        private final int guests;
        private final List<Integer> options;
        private final boolean retained;

        private Slot(int id, int typeId, String typeName, Integer assigned, LocalDate checkOut, int capacity,
                int guests, List<Integer> options, boolean retained) {
            this.id = id;
            this.typeId = typeId;
            this.typeName = typeName;
            this.assigned = assigned;
            this.checkOut = checkOut;
            this.capacity = capacity;
            this.guests = guests;
            this.options = options;
            this.retained = retained;
        }

        public int getId() {
            return id;
        }

        public String getTypeName() {
            return typeName;
        }

        public Integer getAssigned() {
            return assigned;
        }

        public List<Integer> getOptions() {
            return options;
        }

        public boolean isRetained() {
            return retained;
        }
    }

    static Map<String, Object> preview(Connection c, int bookingId) throws SQLException {
        List<Slot> slots = loadSlots(c, bookingId, ReportPeriod.today());
        Map<Integer, Integer> suggested = suggest(slots);
        if (suggested.size() != slots.size()) {
            throw new IllegalArgumentException(NO_ROOM);
        }
        return Map.of("slots", slots, "suggested", suggested,
                "guests", slots.stream().mapToInt(slot -> slot.guests).sum(),
                "capacity", slots.stream().mapToInt(slot -> slot.capacity).sum());
    }

    static void assignRooms(Connection c, int bookingId, int actualGuests, Map<String, String[]> form)
            throws SQLException {
        LocalDate today = ReportPeriod.today();
        List<Slot> slots = loadSlots(c, bookingId, today);
        if (actualGuests < 1 || actualGuests > 1000) {
            throw new IllegalArgumentException("Số khách thực tế không hợp lệ hoặc vượt sức chứa.");
        }

        long supplied = form.keySet().stream().filter(name -> name.startsWith("room-")).count();
        if (supplied != slots.size()) {
            throw new IllegalArgumentException("Chọn đủ số phòng trong đơn.");
        }
        Set<Integer> uniqueRooms = new TreeSet<>();
        Map<Integer, Integer> assignments = new LinkedHashMap<>();
        for (Slot slot : slots) {
            String[] values = form.get("room-" + slot.id);
            int room = roomNumber(values);
            if (room <= 0 || !uniqueRooms.add(room)) {
                throw new IllegalArgumentException("Chọn đủ phòng và chỉ dùng mỗi số phòng một lần.");
            }
            if (!slot.options.contains(room)) {
                throw new IllegalArgumentException(ROOM_CHANGED);
            }
            assignments.put(slot.id, room);
        }

        // Khóa theo thứ tự Phòng trước khi đọc lại điều kiện sẵn sàng.
        try (PreparedStatement ps = c.prepareStatement("SELECT RoomNumber FROM Room WITH(UPDLOCK,HOLDLOCK) WHERE RoomNumber=?")) {
            for (int room : uniqueRooms) {
                ps.setInt(1, room);
                try (ResultSet rows = ps.executeQuery()) {
                    if (!rows.next()) {
                        throw new IllegalArgumentException(ROOM_CHANGED);
                    }
                }
            }
        }
        // Cùng một phòng đã được khóa nên check-in khác phải đợi đến sau khi trạng thái đổi.
        int capacity = 0;
        try (PreparedStatement ps = c.prepareStatement("SELECT t.Capacity FROM Room r "
                + "JOIN RoomType t WITH(HOLDLOCK) ON t.RoomTypeID=r.RoomTypeID "
                + "WHERE r.RoomNumber=? AND " + ELIGIBLE_ROOM)) {
            for (Slot slot : slots) {
                ps.setInt(1, assignments.get(slot.id));
                bindEligibility(ps, 2, slot.typeId, bookingId, today, slot.checkOut);
                try (ResultSet rows = ps.executeQuery()) {
                    if (!rows.next() || rows.getInt(1) < slot.guests) {
                        throw new IllegalArgumentException(ROOM_CHANGED);
                    }
                    capacity += rows.getInt(1);
                }
            }
        }
        if (actualGuests > capacity) {
            throw new IllegalArgumentException("Số khách thực tế vượt sức chứa của các phòng đã chọn.");
        }
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE BookingDetail SET RoomNumber=? WHERE BookingID=? AND BookingDetailID=?")) {
            for (Map.Entry<Integer, Integer> assignment : assignments.entrySet()) {
                ps.setInt(1, assignment.getValue());
                ps.setInt(2, bookingId);
                ps.setInt(3, assignment.getKey());
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không cập nhật được chi tiết nhận phòng.");
                }
            }
        }
    }

    private static int roomNumber(String[] values) {
        if (values == null || values.length != 1) {
            return 0;
        }
        try {
            return Integer.parseInt(values[0]);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static List<Slot> loadSlots(Connection c, int bookingId, LocalDate today)
            throws SQLException {
        String sql = "SELECT d.BookingDetailID,t.RoomTypeID,t.Name,d.RoomNumber,d.CheckInDate,"
                + "d.CheckOutDate,t.Capacity,d.GuestCount FROM BookingDetail d "
                + "LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber "
                + "LEFT JOIN RoomType t ON t.RoomTypeID=COALESCE(d.RoomTypeID,r.RoomTypeID) "
                + "WHERE d.BookingID=? ORDER BY d.BookingDetailID";

        List<Slot> slots = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    LocalDate checkIn = rows.getObject(5, LocalDate.class);
                    LocalDate checkOut = rows.getObject(6, LocalDate.class);
                    if (!today.equals(checkIn)) {
                        throw new IllegalArgumentException("Chỉ nhận phòng vào đúng ngày đã đặt.");
                    }
                    if (checkOut == null || !checkOut.isAfter(checkIn) || rows.getInt(2) == 0
                            || rows.getInt(8) > rows.getInt(7)) {
                        throw new IllegalArgumentException("Chi tiết thiếu loại phòng, vượt sức chứa hoặc có lịch không hợp lệ.");
                    }
                    slots.add(new Slot(rows.getInt(1), rows.getInt(2), rows.getString(3), rows.getObject(4, Integer.class),
                            checkOut, rows.getInt(7), rows.getInt(8), List.of(), false));
                }
            }
        }
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("Đơn không có chi tiết phòng để nhận.");
        }

        Map<Integer, Map<LocalDate, List<Integer>>> optionsByType = new HashMap<>();
        Set<Integer> retainedRooms = new HashSet<>();
        List<Slot> availableSlots = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT r.RoomNumber FROM Room r WHERE "
                + ELIGIBLE_ROOM + " ORDER BY r.RoomNumber")) {
            for (Slot slot : slots) {
                Map<LocalDate, List<Integer>> optionsByCheckout = optionsByType.computeIfAbsent(
                        slot.typeId, type -> new HashMap<>());
                List<Integer> options = optionsByCheckout.get(slot.checkOut);
                if (options == null) {
                    options = new ArrayList<>();
                    bindEligibility(ps, 1, slot.typeId, bookingId, today, slot.checkOut);
                    try (ResultSet rows = ps.executeQuery()) {
                        while (rows.next()) {
                            options.add(rows.getInt(1));
                        }
                    }
                    optionsByCheckout.put(slot.checkOut, options);
                }
                boolean retained = slot.assigned != null && options.contains(slot.assigned) && retainedRooms.add(slot.assigned);
                availableSlots.add(new Slot(slot.id, slot.typeId, slot.typeName, slot.assigned, slot.checkOut,
                        slot.capacity, slot.guests, options, retained));
            }
        }

        List<Slot> result = new ArrayList<>();
        for (Slot slot : availableSlots) {
            List<Integer> options = slot.retained ? List.of(slot.assigned) : slot.options.stream()
                    .filter(room -> !retainedRooms.contains(room)).collect(java.util.stream.Collectors.toList());
            result.add(new Slot(slot.id, slot.typeId, slot.typeName, slot.assigned, slot.checkOut,
                    slot.capacity, slot.guests, options, slot.retained));
        }
        return result;
    }

    private static void bindEligibility(PreparedStatement ps, int start, int typeId, int bookingId,
            LocalDate from, LocalDate to) throws SQLException {
        ps.setInt(start, typeId);
        ps.setInt(start + 1, bookingId);
        ps.setObject(start + 2, to);
        ps.setObject(start + 3, from);
    }

    private static Map<Integer, Integer> suggest(List<Slot> slots) {
        Map<Integer, Integer> selected = new LinkedHashMap<>();
        Map<Integer, List<Slot>> byType = new LinkedHashMap<>();
        for (Slot slot : slots) {
            if (slot.retained) {
                selected.put(slot.id, slot.assigned);
            } else {
                byType.computeIfAbsent(slot.typeId, type -> new ArrayList<>()).add(slot);
            }
        }
        for (List<Slot> group : byType.values()) {
            group.sort(Comparator.comparing((Slot slot) -> slot.checkOut).reversed().thenComparingInt(slot -> slot.id));
            List<Integer> pool = group.get(group.size() - 1).options;
            Map<Integer, Integer> best = Map.of();
            int bestSpan = Integer.MAX_VALUE;
            boolean sameOptions = group.get(0).options.equals(pool);
            for (int start = 0; start + group.size() <= pool.size(); start++) {
                Map<Integer, Integer> choice = new LinkedHashMap<>();
                if (sameOptions) {
                    for (int i = 0; i < group.size(); i++) {
                        choice.put(group.get(i).id, pool.get(start + i));
                    }
                } else {
                    Set<Integer> used = new HashSet<>();
                    int first = pool.get(start);
                    for (Slot slot : group) {
                        // Kỳ dài hơn có tập phòng con, nên luôn chọn cho dòng đó trước.
                        for (int room : slot.options) {
                            if (room >= first && used.add(room)) {
                                choice.put(slot.id, room);
                                break;
                            }
                        }
                    }
                }
                if (choice.size() != group.size()) {
                    continue;
                }
                int low = Integer.MAX_VALUE;
                int high = 0;
                for (int room : selected.values()) {
                    low = Math.min(low, room);
                    high = Math.max(high, room);
                }
                for (int room : choice.values()) {
                    low = Math.min(low, room);
                    high = Math.max(high, room);
                }
                if (high - low < bestSpan) {
                    bestSpan = high - low;
                    best = choice;
                }
            }
            selected.putAll(best);
        }
        return selected;
    }
}
