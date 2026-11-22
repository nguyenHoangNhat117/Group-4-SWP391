package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.RoomOccupancySql;

final class RoomTypeInventory {
    private RoomTypeInventory() { }

    static int available(Connection conn, int typeId, LocalDate from, LocalDate to,
            boolean requireClean, int physicalFree) throws SQLException {
        if (physicalFree <= 0) {
            return 0;
        }
        Stay target = new Stay(from, to);
        List<Stay> held = overlappingHolds(conn, typeId, target);
        if (held.isEmpty()) {
            return physicalFree;
        }
        List<Stay> window = new ArrayList<>(held);
        window.add(target);
        Map<Integer, List<Stay>> rooms = loadRooms(conn, typeId, window, requireClean);
        int low = 0;
        int high = physicalFree;
        while (low < high) {
            int count = low + (high - low + 1) / 2;
            List<Stay> combined = new ArrayList<>(held);
            for (int i = 0; i < count; i++) {
                combined.add(target);
            }
            if (fits(combined, rooms)) {
                low = count;
            } else {
                high = count - 1;
            }
        }
        return low;
    }

    static boolean canAssign(Connection conn, int typeId, int roomNumber, LocalDate from, LocalDate to,
            int assignedDetailId) throws SQLException {
        Stay stay = new Stay(from, to);
        List<Stay> held = overlappingHolds(conn, typeId, stay);
        if (assignedDetailId > 0) {
            held.removeIf(booking -> booking.id == assignedDetailId);
        }
        if (held.isEmpty()) {
            return true;
        }
        Map<Integer, List<Stay>> rooms = loadRooms(conn, typeId, held, true);
        List<Stay> schedule = rooms.get(roomNumber);
        if (schedule != null) {
            schedule.add(stay);
        }
        return fits(held, rooms);
    }

    private static List<Stay> overlappingHolds(Connection conn, int typeId, Stay target) throws SQLException {
        String sql = "SELECT d.BookingDetailID,d.CheckInDate,d.CheckOutDate FROM BookingDetail d "
                + "JOIN Booking b ON b.BookingID=d.BookingID WHERE d.RoomTypeID=? AND d.RoomNumber IS NULL "
                + "AND b.Status IN ('pending','confirmed') AND " + RoomOccupancySql.active("b");
        List<Stay> available = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    available.add(new Stay(rows.getObject(2, LocalDate.class), rows.getObject(3, LocalDate.class), rows.getInt(1)));
                }
            }
        }
        List<Stay> component = new ArrayList<>();
        Stay window = target;
        boolean expanded;
        do {
            expanded = false;
            for (var iterator = available.iterator(); iterator.hasNext();) {
                Stay candidate = iterator.next();
                if (candidate.overlaps(window)) {
                    component.add(candidate);
                    iterator.remove();
                    if (candidate.from.isBefore(window.from) || candidate.to.isAfter(window.to)) {
                        window = new Stay(candidate.from.isBefore(window.from) ? candidate.from : window.from,
                                candidate.to.isAfter(window.to) ? candidate.to : window.to);
                        expanded = true;
                    }
                }
            }
        } while (expanded);
        return component;
    }

    private static Map<Integer, List<Stay>> loadRooms(Connection conn, int typeId, List<Stay> held,
            boolean requireClean) throws SQLException {
        LocalDate earliest = held.stream().map(stay -> stay.from).min(Comparator.naturalOrder()).orElseThrow();
        LocalDate latest = held.stream().map(stay -> stay.to).max(Comparator.naturalOrder()).orElseThrow();
        String end = RoomOccupancySql.endDate("b", "d");
        String sql = "SELECT r.RoomNumber,stay.FromDate,stay.ToDate FROM Room r LEFT JOIN "
                + "(SELECT d.RoomNumber,d.CheckInDate AS FromDate," + end + " AS ToDate FROM BookingDetail d "
                + "JOIN Booking b ON b.BookingID=d.BookingID WHERE d.RoomNumber IS NOT NULL AND "
                + RoomOccupancySql.overlaps("b", "d") + ") stay ON stay.RoomNumber=r.RoomNumber "
                + "WHERE r.RoomTypeID=? AND r.Status='available' AND (?=0 OR r.NeedsCleaning=0)";
        Map<Integer, List<Stay>> rooms = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, latest);
            ps.setObject(2, earliest);
            ps.setInt(3, typeId);
            ps.setBoolean(4, requireClean);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    List<Stay> schedule = rooms.computeIfAbsent(rows.getInt(1), ignored -> new ArrayList<>());
                    LocalDate from = rows.getObject(2, LocalDate.class);
                    if (from != null) {
                        schedule.add(new Stay(from, rows.getObject(3, LocalDate.class)));
                    }
                }
            }
        }
        return rooms;
    }

    private static boolean fits(List<Stay> pending, Map<Integer, List<Stay>> rooms) {
        if (pending.isEmpty()) {
            return true;
        }
        int constrained = 0;
        List<List<Stay>> options = null;
        for (int index = 0; index < pending.size(); index++) {
            Stay stay = pending.get(index);
            List<List<Stay>> candidates = new ArrayList<>();
            for (List<Stay> schedule : rooms.values()) {
                if (schedule.stream().noneMatch(stay::overlaps) && !candidates.contains(schedule)) {
                    candidates.add(schedule);
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
        Stay next = pending.remove(constrained);
        try {
            for (List<Stay> schedule : options) {
                schedule.add(next);
                boolean fits = fits(pending, rooms);
                schedule.remove(schedule.size() - 1);
                if (fits) {
                    return true;
                }
            }
            return false;
        } finally {
            pending.add(constrained, next);
        }
    }

    private static final class Stay {
        final LocalDate from;
        final LocalDate to;
        final int id;

        Stay(LocalDate from, LocalDate to) {
            this(from, to, 0);
        }

        Stay(LocalDate from, LocalDate to, int id) {
            this.from = from;
            this.to = to;
            this.id = id;
        }

        boolean overlaps(Stay other) {
            return from.isBefore(other.to) && other.from.isBefore(to);
        }
    }
}
