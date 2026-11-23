package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.RoomOccupancySql;

/** Kỳ lưu trú dùng khoảng [ngày nhận, ngày trả). */
final class RoomTypeInventory {
    private final Map<Integer, List<Stay>> rooms = new LinkedHashMap<>();
    private final List<Stay> unassigned = new ArrayList<>();

    static RoomTypeInventory load(Connection conn, int typeId, LocalDate from, Integer removedRoom)
            throws SQLException {
        RoomTypeInventory inventory = new RoomTypeInventory();
        String sql = "SELECT RoomNumber FROM Room WHERE RoomTypeID=? AND Status='available' ORDER BY RoomNumber";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    int room = rows.getInt(1);
                    if (!Integer.valueOf(room).equals(removedRoom)) {
                        inventory.rooms.put(room, new ArrayList<>());
                    }
                }
            }
        }
        String end = RoomOccupancySql.endDate("b", "d");
        sql = "SELECT d.BookingDetailID,d.RoomNumber,d.CheckInDate," + end + " AS EndDate,"
                + "CASE WHEN d.IsProvisionalRoom=1 AND b.Status IN ('pending','confirmed') THEN 1 ELSE 0 END "
                + "FROM BookingDetail d JOIN Booking b ON b.BookingID=d.BookingID "
                + "LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber WHERE COALESCE(d.RoomTypeID,r.RoomTypeID)=? "
                + "AND " + RoomOccupancySql.active("b") + " AND " + end + ">? "
                + "AND " + end + ">d.CheckInDate ORDER BY d.CheckInDate,d.BookingDetailID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            ps.setObject(2, from);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    Integer room = rows.getObject(2, Integer.class);
                    boolean moving = room != null && room.equals(removedRoom) && rows.getBoolean(5);
                    Stay stay = new Stay(rows.getObject(3, LocalDate.class), rows.getObject(4, LocalDate.class),
                            rows.getInt(1), moving);
                    if (room == null || moving) {
                        inventory.unassigned.add(stay);
                    } else if (inventory.rooms.containsKey(room)) {
                        inventory.rooms.get(room).add(stay);
                    }
                }
            }
        }
        return inventory;
    }

    int available(LocalDate from, LocalDate to) {
        Stay stay = new Stay(from, to, 0, false);
        int high = (int) rooms.values().stream().filter(schedule -> isFree(schedule, stay)).count();
        int low = 0;
        while (low < high) {
            int quantity = low + (high - low + 1) / 2;
            if (reserve(from, to, quantity).size() == quantity) {
                low = quantity;
            } else {
                high = quantity - 1;
            }
        }
        return low;
    }

    List<Integer> reserve(LocalDate from, LocalDate to, int quantity) {
        Stay target = new Stay(from, to, 0, false);
        if (rooms.values().stream().filter(schedule -> isFree(schedule, target)).count() < quantity) {
            return List.of();
        }
        List<Stay> requested = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            requested.add(new Stay(from, to, 0, false));
        }
        List<Stay> pending = connectedHolds(from, to);
        pending.addAll(requested);
        Map<Stay, Integer> plan = new LinkedHashMap<>();
        if (!assign(pending, plan)) {
            return List.of();
        }
        List<Integer> result = new ArrayList<>();
        for (Stay stay : requested) {
            result.add(plan.get(stay));
        }
        return result;
    }

    boolean canKeepHolds(LocalDate from, LocalDate to) {
        return assign(connectedHolds(from, to), new LinkedHashMap<>());
    }

    Stay moveHolds(Connection conn, int removedRoom) throws SQLException {
        Map<Stay, Integer> plan = new LinkedHashMap<>();
        Set<Stay> included = new HashSet<>();
        for (Stay hold : unassigned) {
            if (included.contains(hold)) {
                continue;
            }
            List<Stay> group = connectedHolds(hold.from, hold.to);
            included.addAll(group);
            if (!assign(group, plan)) {
                return hold;
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("UPDATE BookingDetail SET RoomNumber=? "
                + "WHERE BookingDetailID=? AND RoomNumber=? AND IsProvisionalRoom=1")) {
            for (Map.Entry<Stay, Integer> assignment : plan.entrySet()) {
                Stay stay = assignment.getKey();
                if (stay.moving) {
                    ps.setInt(1, assignment.getValue());
                    ps.setInt(2, stay.id);
                    ps.setInt(3, removedRoom);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Provisional room changed while relocating it");
                    }
                }
            }
        }
        return null;
    }

    private List<Stay> connectedHolds(LocalDate from, LocalDate to) {
        List<Stay> remaining = new ArrayList<>(unassigned);
        List<Stay> connected = new ArrayList<>();
        boolean expanded;
        do {
            expanded = false;
            for (var iterator = remaining.iterator(); iterator.hasNext();) {
                Stay stay = iterator.next();
                if (stay.from.isBefore(to) && from.isBefore(stay.to)) {
                    connected.add(stay);
                    iterator.remove();
                    if (stay.from.isBefore(from)) {
                        from = stay.from;
                        expanded = true;
                    }
                    if (stay.to.isAfter(to)) {
                        to = stay.to;
                        expanded = true;
                    }
                }
            }
        } while (expanded);
        return connected;
    }

    private boolean assign(List<Stay> pending, Map<Stay, Integer> plan) {
        if (pending.isEmpty()) {
            return true;
        }
        int constrained = 0;
        List<Integer> options = null;
        for (int index = 0; index < pending.size(); index++) {
            Stay stay = pending.get(index);
            List<Integer> candidates = new ArrayList<>();
            Set<List<Stay>> equivalent = new HashSet<>();
            for (Map.Entry<Integer, List<Stay>> room : rooms.entrySet()) {
                List<Stay> schedule = room.getValue();
                if (isFree(schedule, stay) && equivalent.add(schedule)) {
                    candidates.add(room.getKey());
                }
            }
            if (candidates.isEmpty()) {
                return false;
            }
            if (options == null || candidates.size() < options.size()) {
                options = candidates;
                constrained = index;
            }
        }
        Stay stay = pending.remove(constrained);
        try {
            for (int room : options) {
                List<Stay> schedule = rooms.get(room);
                schedule.add(stay);
                plan.put(stay, room);
                boolean fits = assign(pending, plan);
                schedule.remove(schedule.size() - 1);
                if (fits) {
                    return true;
                }
                plan.remove(stay);
            }
            return false;
        } finally {
            pending.add(constrained, stay);
        }
    }

    private static boolean isFree(List<Stay> schedule, Stay stay) {
        return schedule.stream().noneMatch(other -> stay.from.isBefore(other.to) && other.from.isBefore(stay.to));
    }

    static final class Stay {
        final LocalDate from;
        final LocalDate to;
        private final int id;
        private final boolean moving;

        private Stay(LocalDate from, LocalDate to, int id, boolean moving) {
            this.from = from;
            this.to = to;
            this.id = id;
            this.moving = moving;
        }
    }
}
