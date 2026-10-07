package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Room;
import model.RoomType;

public class RoomDAO extends DBContext {

    private static final int PAGE_SIZE = 5;

    /*
     * =========================================================
     * 1. GET TOTAL ROOM PAGES
     * Used by Admin Room Management.
     * =========================================================
     */
    public int getTotalPages() {

        String sql =
                "SELECT COUNT(*) AS TotalRooms "
                + "FROM Room";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                int totalRooms =
                        rs.getInt("TotalRooms");

                return (int) Math.ceil(
                        totalRooms * 1.0 / PAGE_SIZE
                );
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 2. GET ALL ROOMS
     * =========================================================
     */
    public List<Room> getAll() {

        List<Room> rooms =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "r.RoomNumber, "
                + "r.Status, "
                + "rt.RoomTypeID, "
                + "rt.Name, "
                + "rt.Description, "
                + "rt.PricePerNight, "
                + "rt.Beds, "
                + "rt.Capacity, "
                + "rt.Picture "
                + "FROM Room r "
                + "JOIN RoomType rt "
                + "ON rt.RoomTypeID = r.RoomTypeID "
                + "ORDER BY r.RoomNumber";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                rooms.add(
                        mapRoom(rs)
                );
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    /*
     * =========================================================
     * 3. GET PAGINATED ROOM LIST
     * Admin management.
     * =========================================================
     */
    public List<Room> getPage(int index) {

        List<Room> rooms =
                new ArrayList<>();

        if (index < 1) {
            index = 1;
        }

        String sql =
                "SELECT "
                + "r.RoomNumber, "
                + "r.Status, "
                + "rt.RoomTypeID, "
                + "rt.Name, "
                + "rt.Description, "
                + "rt.PricePerNight, "
                + "rt.Beds, "
                + "rt.Capacity, "
                + "rt.Picture "
                + "FROM Room r "
                + "JOIN RoomType rt "
                + "ON rt.RoomTypeID = r.RoomTypeID "
                + "ORDER BY r.RoomNumber "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    (index - 1) * PAGE_SIZE
            );

            ps.setInt(
                    2,
                    PAGE_SIZE
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    rooms.add(
                            mapRoom(rs)
                    );
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    /*
     * =========================================================
     * 4. GET ALL AVAILABLE ROOMS FOR DATE RANGE
     *
     * Availability rule:
     *
     * room status = available
     * room type is active
     * no non-cancelled BookingDetail overlaps
     * requested date range
     * =========================================================
     */
    public List<Room> getAvailableRooms(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        List<Room> rooms =
                new ArrayList<>();

        if (!isValidDateRange(
                checkInDate,
                checkOutDate)) {

            return rooms;
        }

        String sql =
                "SELECT "
                + "r.RoomNumber, "
                + "r.Status, "
                + "rt.RoomTypeID, "
                + "rt.Name, "
                + "rt.Description, "
                + "rt.PricePerNight, "
                + "rt.Beds, "
                + "rt.Capacity, "
                + "rt.Picture "
                + "FROM Room r "
                + "JOIN RoomType rt "
                + "ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.Status = 'available' "
                + "AND rt.IsActive = 1 "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b "
                + "    ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND b.Status <> 'cancelled' "
                + "    AND bd.CheckInDate < ? "
                + "    AND bd.CheckOutDate > ? "
                + ") "
                + "ORDER BY r.RoomNumber";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            /*
             * Existing CheckIn < requested CheckOut
             * Existing CheckOut > requested CheckIn
             */
            ps.setObject(
                    1,
                    checkOutDate
            );

            ps.setObject(
                    2,
                    checkInDate
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    rooms.add(
                            mapRoom(rs)
                    );
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    /*
     * =========================================================
     * 5. AVAILABLE ROOM PAGINATION
     * =========================================================
     */
    public List<Room> getAvailableRoomPage(
            int index,
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        List<Room> rooms =
                new ArrayList<>();

        if (index < 1) {
            index = 1;
        }

        if (!isValidDateRange(
                checkInDate,
                checkOutDate)) {

            return rooms;
        }

        String sql =
                "SELECT "
                + "r.RoomNumber, "
                + "r.Status, "
                + "rt.RoomTypeID, "
                + "rt.Name, "
                + "rt.Description, "
                + "rt.PricePerNight, "
                + "rt.Beds, "
                + "rt.Capacity, "
                + "rt.Picture "
                + "FROM Room r "
                + "JOIN RoomType rt "
                + "ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.Status = 'available' "
                + "AND rt.IsActive = 1 "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b "
                + "    ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND b.Status <> 'cancelled' "
                + "    AND bd.CheckInDate < ? "
                + "    AND bd.CheckOutDate > ? "
                + ") "
                + "ORDER BY r.RoomNumber "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setObject(
                    1,
                    checkOutDate
            );

            ps.setObject(
                    2,
                    checkInDate
            );

            ps.setInt(
                    3,
                    (index - 1) * PAGE_SIZE
            );

            ps.setInt(
                    4,
                    PAGE_SIZE
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    rooms.add(
                            mapRoom(rs)
                    );
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    /*
     * =========================================================
     * 6. COUNT AVAILABLE ROOMS
     *
     * Useful for correct pagination after search.
     * =========================================================
     */
    public int countAvailableRooms(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        if (!isValidDateRange(
                checkInDate,
                checkOutDate)) {

            return 0;
        }

        String sql =
                "SELECT COUNT(*) AS Total "
                + "FROM Room r "
                + "JOIN RoomType rt "
                + "ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.Status = 'available' "
                + "AND rt.IsActive = 1 "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b "
                + "    ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND b.Status <> 'cancelled' "
                + "    AND bd.CheckInDate < ? "
                + "    AND bd.CheckOutDate > ? "
                + ")";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setObject(
                    1,
                    checkOutDate
            );

            ps.setObject(
                    2,
                    checkInDate
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 7. GET ROOM BY ROOM NUMBER
     * =========================================================
     */
    public Room getRoomByNumber(
            int roomNumber) {

        String sql =
                "SELECT "
                + "r.RoomNumber, "
                + "r.Status, "
                + "rt.RoomTypeID, "
                + "rt.Name, "
                + "rt.Description, "
                + "rt.PricePerNight, "
                + "rt.Beds, "
                + "rt.Capacity, "
                + "rt.Picture "
                + "FROM Room r "
                + "JOIN RoomType rt "
                + "ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.RoomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomNumber
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {
                    return mapRoom(rs);
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /*
     * =========================================================
     * 8. CHECK ROOM EXISTS
     * =========================================================
     */
    public boolean doesRoomExist(
            int roomNumber) {

        String sql =
                "SELECT 1 "
                + "FROM Room "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomNumber
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /*
     * =========================================================
     * 9. CREATE ROOM
     *
     * IMPORTANT:
     * RoomNumber in your new database is IDENTITY(100,1).
     *
     * Therefore Admin should NOT manually supply
     * RoomNumber when creating a room.
     *
     * Method returns generated RoomNumber.
     * Returns -1 if creation fails.
     * =========================================================
     */
    public int create(
            String status,
            int roomTypeId) {

        if (!isValidRoomStatus(status)) {
            return -1;
        }

        String sql =
                "INSERT INTO Room "
                + "(RoomTypeID, Status) "
                + "VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            ps.setInt(
                    1,
                    roomTypeId
            );

            ps.setString(
                    2,
                    status
            );

            int affectedRows =
                    ps.executeUpdate();

            if (affectedRows == 0) {
                return -1;
            }

            try (ResultSet rs =
                    ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    /*
     * =========================================================
     * 10. UPDATE ROOM
     *
     * RoomNumber is primary key / identity.
     * We do not change the room number.
     * =========================================================
     */
    public int update(
            int roomNumber,
            String status,
            int roomTypeId) {

        if (!isValidRoomStatus(status)) {
            return 0;
        }

        String sql =
                "UPDATE Room "
                + "SET "
                + "RoomTypeID = ?, "
                + "Status = ? "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomTypeId
            );

            ps.setString(
                    2,
                    status
            );

            ps.setInt(
                    3,
                    roomNumber
            );

            return ps.executeUpdate();

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 11. CHECK WHETHER ROOM HAS BOOKING HISTORY
     * =========================================================
     */
    public boolean hasBookingHistory(
            int roomNumber) {

        String sql =
                "SELECT 1 "
                + "FROM BookingDetail "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomNumber
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        /*
         * Safer to assume history exists
         * when DB checking fails.
         */
        return true;
    }

    /*
     * =========================================================
     * 12. DELETE ROOM
     *
     * RDS requires booking history to remain intact.
     *
     * If the room has BookingDetail history:
     * do NOT physically delete it.
     *
     * Instead mark it out_of_service.
     * =========================================================
     */
    public int delete(
            int roomNumber) {

        if (!doesRoomExist(roomNumber)) {
            return 0;
        }

        /*
         * Room already appeared in a booking.
         * Preserve historical foreign-key references.
         */
        if (hasBookingHistory(roomNumber)) {

            return setRoomOutOfService(
                    roomNumber
            );
        }

        /*
         * Room has never been booked.
         * Physical delete is safe.
         */
        String sql =
                "DELETE FROM Room "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomNumber
            );

            return ps.executeUpdate();

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 13. SOFT REMOVE / DISABLE ROOM
     * =========================================================
     */
    public int setRoomOutOfService(
            int roomNumber) {

        String sql =
                "UPDATE Room "
                + "SET Status = 'out_of_service' "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomNumber
            );

            return ps.executeUpdate();

        } catch (SQLException ex) {

            Logger.getLogger(
                    RoomDAO.class.getName()
            ).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 14. CHECK ROOM STATUS
     *
     * Must match HotelDB CHECK constraint:
     *
     * available
     * maintenance
     * out_of_service
     * =========================================================
     */
    private boolean isValidRoomStatus(
            String status) {

        if (status == null) {
            return false;
        }

        return status.equals("available")
                || status.equals("maintenance")
                || status.equals("out_of_service");
    }

    /*
     * =========================================================
     * 15. DATE VALIDATION
     * =========================================================
     */
    private boolean isValidDateRange(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        if (checkInDate == null
                || checkOutDate == null) {

            return false;
        }

        return checkOutDate.isAfter(
                checkInDate
        );
    }

    /*
     * =========================================================
     * 16. MAP RESULTSET -> ROOM
     * =========================================================
     */
    private Room mapRoom(
            ResultSet rs)
            throws SQLException {

        RoomType roomType =
                new RoomType(
                        rs.getInt(
                                "RoomTypeID"
                        ),
                        rs.getString(
                                "Name"
                        ),
                        rs.getString(
                                "Description"
                        ),
                        rs.getBigDecimal(
                                "PricePerNight"
                        ),
                        rs.getInt(
                                "Beds"
                        ),
                        rs.getInt(
                                "Capacity"
                        ),
                        rs.getString(
                                "Picture"
                        )
                );

        return new Room(
                rs.getInt(
                        "RoomNumber"
                ),
                rs.getString(
                        "Status"
                ),
                roomType
        );
    }
}