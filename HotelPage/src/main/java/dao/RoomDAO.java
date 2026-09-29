/*
 * RoomDAO.java
 * Data Access Object class to handle operations related to rooms in the hotel system.
 * Extends DBContext to manage database connectivity.
 * 
 * Author: Đặng Hoàng Vũ
 */

package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Room;
import model.RoomType;

public class RoomDAO extends DBContext {

    /**
     * Calculates the total number of pages required to display all rooms,
     * assuming each page displays 5 rooms.
     */
    public int getTotalPages() {
        try {
            String query = "SELECT CEILING(COUNT(*) * 1.0 / 5) AS TotalPages FROM Room r;";
            PreparedStatement pstatement = this.getConnection().prepareStatement(query);
            ResultSet rs = pstatement.executeQuery();

            if (rs.next()) {
                return rs.getInt(1); // Get total pages from first column
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Retrieves all rooms with their corresponding room types.
     * @return List of Room objects.
     */
    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();
        try {
            String query = "SELECT RoomNumber, Status, r.RoomTypeID, Name, Description, PricePerNight, Beds, Capacity, Picture "
                         + "FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID;";
            PreparedStatement ps = this.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Extract room and room type data
                RoomType roomType = new RoomType(
                    rs.getInt(3), rs.getString(4), rs.getString(5),
                    rs.getBigDecimal(6), rs.getInt(7),
                    rs.getInt(8), rs.getString(9)
                );
                Room room = new Room(rs.getInt(1), rs.getString(2), roomType);
                rooms.add(room);
            }

        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rooms;
    }

    /**
     * Retrieves a specific page of rooms for pagination.
     * Each page contains 5 rooms.
     * @param index Page index (starting from 1).
     */
    public List<Room> getPage(int index) {
        List<Room> rooms = new ArrayList<>();
        try {
            String query = "SELECT * FROM ("
                         + "SELECT ROW_NUMBER() OVER (ORDER BY RoomNumber ASC) AS i, RoomNumber, Status, r.RoomTypeID, "
                         + "Name, Description, PricePerNight, Beds, Capacity, Picture "
                         + "FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID"
                         + ") AS Rooms WHERE i BETWEEN ? AND ?";
            Object[] params = { (index - 1) * 5 + 1, index * 5 };
            ResultSet rs = this.executeSelectionQuery(query, params);

            while (rs.next()) {
                RoomType roomType = new RoomType(
                    rs.getInt(4), rs.getString(5), rs.getString(6),
                    rs.getBigDecimal(7), rs.getInt(8),
                    rs.getInt(9), rs.getString(10)
                );
                Room room = new Room(rs.getInt(2), rs.getString(3), roomType);
                rooms.add(room);
            }

        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rooms;
    }

    /**
     * Retrieves a specific page of available rooms within a date range.
     */
    public List<Room> getAvailableRoomPage(int index, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = new ArrayList<>();
        try {
            String query = "SELECT * FROM ("
                         + "SELECT ROW_NUMBER() OVER (ORDER BY RoomNumber ASC) AS i, "
                         + "r.RoomNumber, r.Status, r.RoomTypeID, rt.Name, rt.Description, rt.PricePerNight, "
                         + "rt.Beds, rt.Capacity, rt.Picture "
                         + "FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID "
                         + "WHERE r.Status = 'available' AND r.RoomNumber NOT IN ("
                         + "SELECT RoomNumber FROM Booking WHERE NOT (CheckOutDate <= ? OR CheckInDate >= ?)"
                         + ")) AS AvailableRooms WHERE i BETWEEN ? AND ?";
            Object[] params = { checkInDate, checkOutDate, (index - 1) * 5 + 1, index * 5 };
            ResultSet rs = this.executeSelectionQuery(query, params);

            while (rs.next()) {
                RoomType roomType = new RoomType(
                    rs.getInt(4), rs.getString(5), rs.getString(6),
                    rs.getBigDecimal(7), rs.getInt(8),
                    rs.getInt(9), rs.getString(10)
                );
                Room room = new Room(rs.getInt(2), rs.getString(3), roomType);
                availableRooms.add(room);
            }

        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return availableRooms;
    }

    /**
     * Retrieves all available rooms (without pagination) for a given date range.
     */
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = new ArrayList<>();
        try {
            String query = "SELECT r.RoomNumber, r.Status, r.RoomTypeID, rt.Name, rt.Description, "
                         + "rt.PricePerNight, rt.Beds, rt.Capacity, rt.Picture "
                         + "FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID "
                         + "WHERE r.Status = 'available' AND r.RoomNumber NOT IN ("
                         + "SELECT RoomNumber FROM Booking WHERE NOT (CheckOutDate <= ? OR CheckInDate >= ?))";
            Object[] params = { checkInDate, checkOutDate };
            ResultSet rs = this.executeSelectionQuery(query, params);

            while (rs.next()) {
                RoomType roomType = new RoomType(
                    rs.getInt("RoomTypeID"), rs.getString("Name"), rs.getString("Description"),
                    rs.getBigDecimal("PricePerNight"), rs.getInt("Beds"),
                    rs.getInt("Capacity"), rs.getString("Picture")
                );
                Room room = new Room(rs.getInt("RoomNumber"), rs.getString("Status"), roomType);
                availableRooms.add(room);
            }

        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return availableRooms;
    }

    /**
     * Retrieves a specific room based on its room number.
     */
    public Room getRoomByNumber(int roomNumber) {
        try {
            String query = "SELECT Status, r.RoomTypeID, Name, Description, PricePerNight, Beds, Capacity, Picture "
                         + "FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID "
                         + "WHERE RoomNumber = ?";
            Object[] params = { roomNumber };
            ResultSet rs = this.executeSelectionQuery(query, params);

            if (rs.next()) {
                RoomType roomType = new RoomType(
                    rs.getInt(2), rs.getString(3), rs.getString(4),
                    rs.getBigDecimal(5), rs.getInt(6),
                    rs.getInt(7), rs.getString(8)
                );
                return new Room(roomNumber, rs.getString(1), roomType);
            }

        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Creates a new room record in the database.
     */
    public int create(int roomNumber, String status, int roomTypeId) {
        try {
            String query = "INSERT INTO Room (RoomNumber, Status, RoomTypeId) VALUES (?, ?, ?);";
            Object[] params = { roomNumber, status, roomTypeId };
            return this.executeQuery(query, params);
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Deletes a room from the database based on its room number.
     */
    public int delete(int roomNumber) {
        try {
            String query = "DELETE FROM Room WHERE RoomNumber = ?;";
            Object[] params = { roomNumber };
            return this.executeQuery(query, params);
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Updates a room's status and type based on its room number.
     */
    public int update(int roomNumber, String status, int roomTypeId) {
        try {
            String query = "UPDATE Room SET Status = ?, RoomTypeID = ? WHERE RoomNumber = ?;";
            Object[] params = { status, roomTypeId, roomNumber };
            return this.executeQuery(query, params);
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Checks if a room with the given room number exists in the database.
     */
    public boolean doesRoomExist(int roomNumber) {
        String sql = "SELECT 1 FROM Room WHERE RoomNumber = ?";
        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{roomNumber});
            return rs.next(); // True if a result exists
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
