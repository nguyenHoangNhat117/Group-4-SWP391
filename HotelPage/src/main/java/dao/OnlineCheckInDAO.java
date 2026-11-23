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
        private final int childCapacity;
        private final int guests;
        private final Integer bookedAdults;
        private final Integer bookedChildren;
        private final boolean provisional;
        private final List<Integer> options;
        private final boolean retained;

        private Slot(int id, int typeId, String typeName, Integer assigned, LocalDate checkOut, int capacity,
                int childCapacity, int guests, Integer bookedAdults, Integer bookedChildren,
                boolean provisional, List<Integer> options, boolean retained) {
            this.id = id;
            this.typeId = typeId;
            this.typeName = typeName;
            this.assigned = assigned;
            this.checkOut = checkOut;
            this.capacity = capacity;
            this.childCapacity = childCapacity;
            this.guests = guests;
            this.bookedAdults = bookedAdults;
            this.bookedChildren = bookedChildren;
            this.provisional = provisional;
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

        public Integer getBookedAdults() { return bookedAdults; }
        public Integer getBookedChildren() { return bookedChildren; }
        public boolean isProvisional() { return provisional; }

        private boolean fitsParty(int maxAdults, int maxChildren) {
            return bookedAdults == null ? guests <= maxAdults
                    : bookedAdults <= maxAdults && bookedChildren <= maxChildren;
        }
    }

    static void lockRoomTypes(Connection c, int bookingId) throws SQLException {
        Set<Integer> types = new TreeSet<>();
        String sql = "SELECT DISTINCT COALESCE(d.RoomTypeID,r.RoomTypeID) FROM BookingDetail d "
                + "LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber WHERE d.BookingID=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    Integer type = rows.getObject(1, Integer.class);
                    if (type != null) {
                        types.add(type);
                    }
                }
            }
        }
        for (int type : types) {
            BookingDAO.lockRoomType(c, type);
        }
    }

    static Map<String, Object> preview(Connection c, int bookingId) throws SQLException {
        List<Slot> slots = loadSlots(c, bookingId, ReportPeriod.today());
        Map<Integer, Integer> suggested = suggest(slots);
        if (suggested.size() != slots.size()) {
            throw new IllegalArgumentException(NO_ROOM);
        }
        BookedParty party = bookedParty(c, bookingId);
        int adultCapacity = slots.stream().mapToInt(slot -> slot.capacity).sum();
        int childCapacity = slots.stream().mapToInt(slot -> slot.childCapacity).sum();
        return Map.of("slots", slots, "suggested", suggested,
                "guests", party == null ? slots.stream().mapToInt(slot -> slot.guests).sum() : party.guests,
                "capacity", checkInCapacity(party, adultCapacity, childCapacity));
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
        int adultCapacity = 0;
        int childCapacity = 0;
        try (PreparedStatement ps = c.prepareStatement("SELECT t.Capacity,t.MaxChildren FROM Room r "
                + "JOIN RoomType t WITH(HOLDLOCK) ON t.RoomTypeID=r.RoomTypeID "
                + "WHERE r.RoomNumber=? AND " + ELIGIBLE_ROOM)) {
            for (Slot slot : slots) {
                ps.setInt(1, assignments.get(slot.id));
                bindEligibility(ps, 2, slot.typeId, bookingId, today, slot.checkOut);
                try (ResultSet rows = ps.executeQuery()) {
                    if (!rows.next() || !slot.fitsParty(rows.getInt(1), rows.getInt(2))) {
                        throw new IllegalArgumentException(ROOM_CHANGED);
                    }
                    adultCapacity += rows.getInt(1);
                    childCapacity += rows.getInt(2);
                }
            }
        }
        if (actualGuests > checkInCapacity(bookedParty(c, bookingId), adultCapacity, childCapacity)) {
            throw new IllegalArgumentException("Số khách thực tế vượt sức chứa của các phòng đã chọn.");
        }
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE BookingDetail SET RoomNumber=?,IsProvisionalRoom=0 WHERE BookingID=? AND BookingDetailID=?")) {
            for (Map.Entry<Integer, Integer> assignment : assignments.entrySet()) {
                ps.setInt(1, assignment.getValue());
                ps.setInt(2, bookingId);
                ps.setInt(3, assignment.getKey());
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không cập nhật được chi tiết nhận phòng.");
                }
            }
        }
        Map<Integer, LocalDate> checkoutByType = new LinkedHashMap<>();
        for (Slot slot : slots) {
            checkoutByType.merge(slot.typeId, slot.checkOut, (first, second) -> first.isAfter(second) ? first : second);
        }
        for (Map.Entry<Integer, LocalDate> group : checkoutByType.entrySet()) {
            RoomTypeInventory inventory = RoomTypeInventory.load(c, group.getKey(), today, null);
            if (!inventory.canKeepHolds(today, group.getValue())) {
                throw new IllegalArgumentException("Phòng đã chọn cần giữ cho đơn khác. Vui lòng chọn phòng khác.");
            }
        }
    }

    private static int checkInCapacity(BookedParty party, int adultCapacity, int childCapacity) {
        return adultCapacity + (party == null ? 0 : Math.min(party.children, childCapacity));
    }

    private static BookedParty bookedParty(Connection c, int bookingId) throws SQLException {
        String sql = "SELECT Adults+Children,Children FROM Booking "
                + "WHERE BookingID=? AND Adults IS NOT NULL AND Children IS NOT NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rows = ps.executeQuery()) {
                return rows.next() ? new BookedParty(rows.getInt(1), rows.getInt(2)) : null;
            }
        }
    }

    private static final class BookedParty {
        private final int guests;
        private final int children;

        private BookedParty(int guests, int children) {
            this.guests = guests;
            this.children = children;
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
                + "d.CheckOutDate,t.Capacity,d.GuestCount,t.MaxChildren,d.BookedAdults,d.BookedChildren,d.IsProvisionalRoom FROM BookingDetail d "
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
                    Slot slot = new Slot(rows.getInt(1), rows.getInt(2), rows.getString(3), rows.getObject(4, Integer.class),
                            checkOut, rows.getInt(7), rows.getInt(9), rows.getInt(8), rows.getObject(10, Integer.class),
                            rows.getObject(11, Integer.class), rows.getBoolean(12), List.of(), false);
                    if (checkOut == null || !checkOut.isAfter(checkIn) || slot.typeId == 0
                            || !slot.fitsParty(slot.capacity, slot.childCapacity)) {
                        throw new IllegalArgumentException("Chi tiết thiếu loại phòng, vượt sức chứa hoặc có lịch không hợp lệ.");
                    }
                    slots.add(slot);
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
                boolean retained = !slot.provisional && slot.assigned != null && options.contains(slot.assigned)
                        && retainedRooms.add(slot.assigned);
                availableSlots.add(new Slot(slot.id, slot.typeId, slot.typeName, slot.assigned, slot.checkOut,
                        slot.capacity, slot.childCapacity, slot.guests, slot.bookedAdults, slot.bookedChildren,
                        slot.provisional, options, retained));
            }
        }

        List<Slot> result = new ArrayList<>();
        for (Slot slot : availableSlots) {
            List<Integer> options = slot.retained ? List.of(slot.assigned) : slot.options.stream()
                    .filter(room -> !retainedRooms.contains(room)).collect(java.util.stream.Collectors.toList());
            result.add(new Slot(slot.id, slot.typeId, slot.typeName, slot.assigned, slot.checkOut,
                    slot.capacity, slot.childCapacity, slot.guests, slot.bookedAdults, slot.bookedChildren,
                    slot.provisional, options, slot.retained));
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
        Map<Integer, Integer> preferred = suggest(slots, true);
        return preferred.size() == slots.size() ? preferred : suggest(slots, false);
    }

    private static Map<Integer, Integer> suggest(List<Slot> slots, boolean preferHeldRooms) {
        Map<Integer, Integer> selected = new LinkedHashMap<>();
        Map<Integer, List<Slot>> byType = new LinkedHashMap<>();
        for (Slot slot : slots) {
            if (slot.retained) {
                selected.put(slot.id, slot.assigned);
            } else if (preferHeldRooms && slot.provisional && slot.assigned != null
                    && slot.options.contains(slot.assigned) && !selected.containsValue(slot.assigned)) {
                selected.put(slot.id, slot.assigned);
            } else {
                byType.computeIfAbsent(slot.typeId, type -> new ArrayList<>()).add(slot);
            }
        }
        for (List<Slot> group : byType.values()) {
            group.sort(Comparator.comparing((Slot slot) -> slot.checkOut).reversed().thenComparingInt(slot -> slot.id));
            List<Integer> pool = group.get(group.size() - 1).options.stream()
                    .filter(room -> !selected.containsValue(room)).collect(java.util.stream.Collectors.toList());
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
                            if (room >= first && !selected.containsValue(room) && used.add(room)) {
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
