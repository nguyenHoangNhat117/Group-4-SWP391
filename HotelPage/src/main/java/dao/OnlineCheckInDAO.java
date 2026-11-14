package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.ReportPeriod;
import util.RoomOccupancySql;

public class OnlineCheckInDAO extends DBContext {
    public enum Result {
        SUCCESS, INVALID_BOOKING, WRONG_DATE, ROOM_NOT_READY, ROOM_OCCUPIED, ROOM_CONFLICT
    }

    public Result checkIn(int bookingId) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                Result result = checkIn(connection, bookingId);
                if (result == Result.SUCCESS) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
                return result;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    private Result checkIn(Connection connection, int bookingId) throws SQLException {
        String bookingSql = "SELECT 1 FROM Booking WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE BookingID=? AND CreatedByUserID IS NULL AND Status='confirmed'";
        try (PreparedStatement statement = connection.prepareStatement(bookingSql)) {
            statement.setInt(1, bookingId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Result.INVALID_BOOKING;
                }
            }
        }

        List<StayRoom> rooms = loadRooms(connection, bookingId);
        if (rooms.isEmpty()) {
            return Result.ROOM_NOT_READY;
        }
        LocalDate today = ReportPeriod.today();
        boolean hasCleaningStatus = hasCleaningStatus(connection);
        for (StayRoom room : rooms) {
            if (room.checkIn == null || room.checkOut == null || !room.checkOut.isAfter(room.checkIn)
                    || !room.checkIn.equals(today)) {
                return Result.WRONG_DATE;
            }
            Result readiness = checkRoom(connection, bookingId, room, today, room.checkOut, hasCleaningStatus);
            if (readiness != Result.SUCCESS) {
                return readiness;
            }
        }

        String updateSql = "UPDATE Booking SET Status='checked_in' WHERE BookingID=? AND Status='confirmed'";
        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setInt(1, bookingId);
            return statement.executeUpdate() == 1 ? Result.SUCCESS : Result.INVALID_BOOKING;
        }
    }

    private List<StayRoom> loadRooms(Connection connection, int bookingId) throws SQLException {
        String sql = "SELECT RoomNumber, CheckInDate, CheckOutDate, GuestCount FROM BookingDetail WITH (HOLDLOCK) "
                + "WHERE BookingID=? ORDER BY RoomNumber";
        List<StayRoom> rooms = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    rooms.add(new StayRoom(rows.getInt(1), rows.getObject(2, LocalDate.class),
                            rows.getObject(3, LocalDate.class), rows.getInt(4)));
                }
            }
        }
        return rooms;
    }

    private boolean hasCleaningStatus(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COL_LENGTH('dbo.Room', 'NeedsCleaning')");
                ResultSet rows = statement.executeQuery()) {
            return rows.next() && rows.getObject(1) != null;
        }
    }

    private Result checkRoom(Connection connection, int bookingId, StayRoom room, LocalDate from, LocalDate to,
            boolean hasCleaningStatus) throws SQLException {
        String roomSql = "SELECT r.Status, rt.Capacity"
                + (hasCleaningStatus ? ", r.NeedsCleaning" : "")
                + " FROM Room r WITH (UPDLOCK,HOLDLOCK) JOIN RoomType rt WITH (HOLDLOCK)"
                + " ON r.RoomTypeID=rt.RoomTypeID WHERE r.RoomNumber=?";
        try (PreparedStatement statement = connection.prepareStatement(roomSql)) {
            statement.setInt(1, room.number);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next() || !"available".equalsIgnoreCase(rows.getString(1))
                        || rows.getInt(2) < room.guests || (hasCleaningStatus && rows.getBoolean(3))) {
                    return Result.ROOM_NOT_READY;
                }
            }
        }

        String conflictSql = "SELECT TOP 1 b.Status FROM BookingDetail d JOIN Booking b WITH (HOLDLOCK)"
                + " ON d.BookingID=b.BookingID WHERE d.RoomNumber=? AND b.BookingID<>?"
                + " AND (b.Status='checked_in' OR (" + RoomOccupancySql.overlaps("b", "d") + "))"
                + " ORDER BY CASE WHEN b.Status='checked_in' THEN 0 ELSE 1 END";
        try (PreparedStatement statement = connection.prepareStatement(conflictSql)) {
            statement.setInt(1, room.number);
            statement.setInt(2, bookingId);
            statement.setObject(3, to);
            statement.setObject(4, from);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Result.SUCCESS;
                }
                return "checked_in".equals(rows.getString(1)) ? Result.ROOM_OCCUPIED : Result.ROOM_CONFLICT;
            }
        }
    }

    private static final class StayRoom {
        final int number;
        final LocalDate checkIn;
        final LocalDate checkOut;
        final int guests;

        StayRoom(int number, LocalDate checkIn, LocalDate checkOut, int guests) {
            this.number = number;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.guests = guests;
        }
    }
}
