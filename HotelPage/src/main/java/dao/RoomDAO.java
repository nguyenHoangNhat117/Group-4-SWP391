package dao;

import util.RoomOccupancySql;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Room;
import model.RoomType;
import util.ReportPeriod;

public class RoomDAO extends DBContext {

    public static final int CHANGE_BLOCKED_BY_BOOKING = -1;
    public static final int MAX_CREATE_QUANTITY = 50;
    private static final String ADMIN_ROOM_FILTER = " WHERE (? IS NULL OR r.RoomTypeID = ?)"
            + " AND (? IS NULL OR r.RoomNumber = ?) ";
    private static final String NO_ACTIVE_BOOKING = "NOT EXISTS ("
            + "SELECT 1 FROM BookingDetail bd WITH (HOLDLOCK) "
            + "JOIN Booking b WITH (HOLDLOCK) ON b.BookingID = bd.BookingID "
            + "WHERE bd.RoomNumber = r.RoomNumber AND (b.Status = 'checked_in' "
            + "OR (b.Status IN ('pending', 'confirmed') AND bd.CheckOutDate > ?)))";

    private static final String TYPE_AVAILABILITY
            = "WITH Nights AS (SELECT CAST(? AS date) AS StayDate "
            + "UNION ALL SELECT DATEADD(day,1,StayDate) FROM Nights "
            + "WHERE DATEADD(day,1,StayDate)<CAST(? AS date)), "
            + "Inventory AS (SELECT rt.RoomTypeID, COUNT(*) AS Total "
            + "FROM RoomType rt JOIN Room r ON r.RoomTypeID=rt.RoomTypeID "
            + "WHERE r.Status='available' AND rt.IsActive=1 AND rt.Capacity>=? "
            + "AND (? IS NULL OR rt.RoomTypeID=?) "
            + "AND (? IS NULL OR rt.PricePerNight>=?) "
            + "AND (? IS NULL OR rt.PricePerNight<?) GROUP BY rt.RoomTypeID), "
            + "Stays AS (SELECT i.RoomTypeID,d.CheckInDate,"
            + RoomOccupancySql.endDate("b", "d") + " AS EffectiveEnd "
            + "FROM BookingDetail d JOIN Booking b ON b.BookingID=d.BookingID "
            + "LEFT JOIN Room legacy ON legacy.RoomNumber=d.RoomNumber AND d.RoomTypeID IS NULL "
            + "JOIN Inventory i ON i.RoomTypeID=COALESCE(d.RoomTypeID,legacy.RoomTypeID) "
            + "WHERE b.Status<>'cancelled' AND d.CheckInDate<? AND "
            + RoomOccupancySql.endDate("b", "d") + ">?), "
            + "Nightly AS (SELECT i.RoomTypeID,n.StayDate, "
            + "CASE WHEN COUNT(s.RoomTypeID)>=i.Total THEN 0 ELSE i.Total-COUNT(s.RoomTypeID) END AS FreeRooms "
            + "FROM Inventory i CROSS JOIN Nights n LEFT JOIN Stays s ON s.RoomTypeID=i.RoomTypeID "
            + "AND s.CheckInDate<=n.StayDate AND s.EffectiveEnd>n.StayDate "
            + "GROUP BY i.RoomTypeID,n.StayDate,i.Total) ";
    private static final String TYPE_AVAILABILITY_TOTALS
            = "SELECT RoomTypeID,MIN(FreeRooms) AS Available FROM Nightly GROUP BY RoomTypeID";
    private static final String INVENTORY_QUERY_END = " OPTION (MAXRECURSION 365)";

    public static final class AvailableType {

        private final RoomType type;
        private final int available;
        private final int reviewCount;
        private final java.math.BigDecimal rating;
        private final Integer readyNow;

        private AvailableType(RoomType type, int available, int reviewCount, java.math.BigDecimal rating, Integer readyNow) {
            this.type = type;
            this.available = available;
            this.reviewCount = reviewCount;
            this.rating = rating;
            this.readyNow = readyNow;
        }

        public RoomType getType() {
            return type;
        }

        public int getAvailable() {
            return available;
        }

        public int getReviewCount() {
            return reviewCount;
        }

        public java.math.BigDecimal getRating() {
            return rating;
        }

        public Integer getReadyNow() {
            return readyNow;
        }

        AvailableType withReadyNow(int count) {
            return new AvailableType(type, available, reviewCount, rating, count);
        }
    }

    public List<AvailableType> findAvailableTypes(LocalDate from, LocalDate to, int guests,
            Integer typeId, model.RoomPriceBand band, String sort) throws SQLException {
        String order;
        switch (sort) {
            case "price_asc":
                order = "rt.PricePerNight ASC, rt.RoomTypeID";
                break;
            case "price_desc":
                order = "rt.PricePerNight DESC, rt.RoomTypeID";
                break;
            case "capacity_desc":
                order = "rt.Capacity DESC, rt.PricePerNight, rt.RoomTypeID";
                break;
            default:
                order = "rt.RoomTypeID";
        }
        String sql = TYPE_AVAILABILITY
                + ", AvailableTypes AS (" + TYPE_AVAILABILITY_TOTALS + ") "
                + "SELECT rt.*,a.Available,reviews.ReviewCount,reviews.Rating "
                + "FROM AvailableTypes a JOIN RoomType rt ON rt.RoomTypeID=a.RoomTypeID CROSS APPLY ("
                + "SELECT COUNT(*) AS ReviewCount, CAST(AVG(rv.Star) AS DECIMAL(3,1)) AS Rating "
                + "FROM Review rv WHERE rv.ReviewStatus='approved' AND EXISTS ("
                + "SELECT 1 FROM BookingDetail bd JOIN Room r ON r.RoomNumber=bd.RoomNumber "
                + "WHERE bd.BookingID=rv.BookingID AND r.RoomTypeID=rt.RoomTypeID)"
                + ") reviews WHERE a.Available>0 ORDER BY " + order + INVENTORY_QUERY_END;
        List<AvailableType> types = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindInventory(ps, from, to, guests, typeId, band);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomType type = new RoomType(rs.getInt("RoomTypeID"), rs.getString("Name"),
                            rs.getString("Description"), rs.getBigDecimal("PricePerNight"),
                            rs.getInt("Beds"), rs.getInt("Capacity"), rs.getString("Picture"));
                    types.add(new AvailableType(type, rs.getInt("Available"),
                            rs.getInt("ReviewCount"), rs.getBigDecimal("Rating"), null));
                }
            }
        }
        return types;
    }

    public int countAvailableRoomsByType(LocalDate from, LocalDate to, int typeId) throws SQLException {
        try (Connection connection = getConnection()) {
            return countAvailableRoomsByType(connection, from, to, typeId, 1);
        }
    }

    static int countAvailableRoomsByType(Connection connection, LocalDate from, LocalDate to,
            int typeId, int guestsPerRoom) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                TYPE_AVAILABILITY + TYPE_AVAILABILITY_TOTALS + INVENTORY_QUERY_END)) {
            bindInventory(ps, from, to, guestsPerRoom, typeId, model.RoomPriceBand.ALL);
            try (ResultSet rows = ps.executeQuery()) {
                return rows.next() ? rows.getInt("Available") : 0;
            }
        }
    }

    public java.util.Map<String, String> findCalendarAvailability(LocalDate from, LocalDate to,
            int guests, Integer typeId, model.RoomPriceBand band, int quantity) throws SQLException {
        String sql = TYPE_AVAILABILITY
                + ", StayLimits AS (SELECT StayDate,FreeRooms, "
                + "MIN(CASE WHEN FreeRooms<? THEN StayDate END) OVER ("
                + "PARTITION BY RoomTypeID ORDER BY StayDate ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING) "
                + "AS FirstFullNight FROM Nightly) "
                + "SELECT StayDate,MAX(COALESCE(FirstFullNight,CAST(? AS date))) AS LastCheckout "
                + "FROM StayLimits WHERE FreeRooms>=? GROUP BY StayDate ORDER BY StayDate" + INVENTORY_QUERY_END;
        java.util.Map<String, String> availability = new java.util.LinkedHashMap<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindInventory(ps, from, to, guests, typeId, band);
            ps.setInt(12, quantity);
            ps.setObject(13, to);
            ps.setInt(14, quantity);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availability.put(rs.getDate("StayDate").toString(), rs.getDate("LastCheckout").toString());
                }
            }
        }
        return availability;
    }

    private static void bindInventory(PreparedStatement ps, LocalDate from, LocalDate to,
            int guests, Integer typeId, model.RoomPriceBand band) throws SQLException {
        ps.setObject(1, from);
        ps.setObject(2, to);
        ps.setInt(3, guests);
        ps.setObject(4, typeId, Types.INTEGER);
        ps.setObject(5, typeId, Types.INTEGER);
        ps.setBigDecimal(6, band.getMinimum());
        ps.setBigDecimal(7, band.getMinimum());
        ps.setBigDecimal(8, band.getUpperExclusive());
        ps.setBigDecimal(9, band.getUpperExclusive());
        ps.setObject(10, to);
        ps.setObject(11, from);
    }

    static int countReadyRooms(Connection connection, int typeId, LocalDate from,
            LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Room r WHERE r.RoomTypeID=? "
                + "AND r.Status='available' AND r.NeedsCleaning=0 AND NOT EXISTS ("
                + "SELECT 1 FROM BookingDetail d JOIN Booking b ON b.BookingID=d.BookingID "
                + "WHERE d.RoomNumber=r.RoomNumber AND (b.Status='checked_in' OR ("
                + RoomOccupancySql.overlaps("b", "d") + ")))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, typeId);
            statement.setObject(2, to);
            statement.setObject(3, from);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }

    private static final int PAGE_SIZE = 6;

    /*
     * =========================================================
     * 1. GET TOTAL ROOM PAGES
     * Used by Admin Room Management.
     * =========================================================
     */
    public int getTotalPages(Integer roomTypeId, Integer roomNumber) throws SQLException {
        return getTotalPages(roomTypeId, roomNumber, null);
    }

    public int getTotalPages(Integer roomTypeId, Integer roomNumber, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Room r" + ADMIN_ROOM_FILTER + " AND (? IS NULL OR r.Status=?) ";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindAdminRoomFilter(ps, roomTypeId, roomNumber);
            ps.setString(5, status);
            ps.setString(6, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return (int) Math.ceil(rs.getInt(1) / (double) PAGE_SIZE);
            }
        }
    }

    private void bindAdminRoomFilter(PreparedStatement ps, Integer roomTypeId, Integer roomNumber)
            throws SQLException {
        ps.setObject(1, roomTypeId, Types.INTEGER);
        ps.setObject(2, roomTypeId, Types.INTEGER);
        ps.setObject(3, roomNumber, Types.INTEGER);
        ps.setObject(4, roomNumber, Types.INTEGER);
    }

    /*
     * =========================================================
     * 2. GET ALL ROOMS
     * =========================================================
     */
    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT "
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

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    /*
     * =========================================================
     * 3. GET PAGINATED ROOM LIST
     * Admin management.
     * =========================================================
     */
    public List<Room> getPage(int index, Integer roomTypeId, Integer roomNumber) throws SQLException {
        return getPage(index, roomTypeId, roomNumber, null, null);
    }

    public List<Room> getPage(int index, Integer roomTypeId, Integer roomNumber, String status, String sort) throws SQLException {
        List<Room> rooms = new ArrayList<>();

        if (index < 1) {
            index = 1;
        }

        String sql = "SELECT "
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
                + ADMIN_ROOM_FILTER + " AND (? IS NULL OR r.Status=?) "
                + "ORDER BY " + ("asc".equals(sort) ? "rt.PricePerNight ASC, " : "desc".equals(sort) ? "rt.PricePerNight DESC, " : "") + "r.RoomNumber "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindAdminRoomFilter(ps, roomTypeId, roomNumber);
            ps.setString(5, status);
            ps.setString(6, status);
            ps.setInt(7, (index - 1) * PAGE_SIZE);
            ps.setInt(8, PAGE_SIZE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
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
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> rooms = new ArrayList<>();

        if (!isValidDateRange(checkInDate, checkOutDate)) {
            return rooms;
        }

        String sql = "SELECT "
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
                + "WHERE r.Status = 'available' AND r.NeedsCleaning=0 "
                + "AND rt.IsActive = 1 "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b "
                + "    ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND " + RoomOccupancySql.overlaps("b", "bd") + " "
                + ") "
                + "ORDER BY r.RoomNumber";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            /*
             * Existing CheckIn < requested CheckOut
             * Existing CheckOut > requested CheckIn
             */
            ps.setObject(1, checkOutDate);
            ps.setObject(2, checkInDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    /*
     * =========================================================
     * 5. AVAILABLE ROOM PAGINATION
     * =========================================================
     */
    public List<Room> getAvailableRoomPage(int index, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> rooms = new ArrayList<>();

        if (index < 1) {
            index = 1;
        }

        if (!isValidDateRange(checkInDate, checkOutDate)) {
            return rooms;
        }

        String sql = "SELECT "
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
                + "WHERE r.Status = 'available' AND r.NeedsCleaning=0 "
                + "AND rt.IsActive = 1 "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b "
                + "    ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND " + RoomOccupancySql.overlaps("b", "bd") + " "
                + ") "
                + "ORDER BY r.RoomNumber "
                + "OFFSET ? ROWS "
                + "FETCH NEXT ? ROWS ONLY";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, checkOutDate);
            ps.setObject(2, checkInDate);
            ps.setInt(3, (index - 1) * PAGE_SIZE);
            ps.setInt(4, PAGE_SIZE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
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
    public int countAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        if (!isValidDateRange(checkInDate, checkOutDate)) {
            return 0;
        }

        String sql = "SELECT COUNT(*) AS Total "
                + "FROM Room r "
                + "JOIN RoomType rt "
                + "ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.Status = 'available' AND r.NeedsCleaning=0 "
                + "AND rt.IsActive = 1 "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b "
                + "    ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND " + RoomOccupancySql.overlaps("b", "bd") + " "
                + ")";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, checkOutDate);
            ps.setObject(2, checkInDate);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 7. GET ROOM BY ROOM NUMBER
     * =========================================================
     */
    public Room getRoomByNumber(int roomNumber) {
        String sql = "SELECT "
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

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRoom(rs);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /*
     * =========================================================
     * 5B. AVAILABLE ROOM PAGINATION WITH FULL UC06 FILTERS
     *
     * BR-14: CheckInDate >= Today AND CheckOutDate > CheckInDate
     * BR-15: Room.Status = 'available', RoomType.IsActive = 1,
     *        RoomType.Capacity >= guestCount, no overlapping non-cancelled BookingDetail
     * BR-16: Optional filter by RoomTypeID and PricePerNight range
     * BR-17: Paginated results
     * =========================================================
     */
    public List<Room> getAvailableRoomPage(
            int index,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int guestCount,
            Integer roomTypeId,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            String sortBy) {

        List<Room> rooms = new ArrayList<>();

        if (index < 1) {
            index = 1;
        }

        if (!isValidDateRange(checkInDate, checkOutDate) || guestCount <= 0) {
            return rooms;
        }

        StringBuilder sql = new StringBuilder(
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
                + "JOIN RoomType rt ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.Status = 'available' AND r.NeedsCleaning=0 "
                + "AND rt.IsActive = 1 "
                + "AND rt.Capacity >= ? "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND " + RoomOccupancySql.overlaps("b", "bd") + " "
                + ") "
        );

        if (roomTypeId != null && roomTypeId > 0) {
            sql.append("AND rt.RoomTypeID = ? ");
        }
        if (minPrice != null) {
            sql.append("AND rt.PricePerNight >= ? ");
        }
        if (maxPrice != null) {
            sql.append("AND rt.PricePerNight <= ? ");
        }

        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY rt.PricePerNight ASC, r.RoomNumber ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY rt.PricePerNight DESC, r.RoomNumber ASC ");
        } else if ("capacity_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY rt.Capacity DESC, rt.PricePerNight ASC, r.RoomNumber ASC ");
        } else {
            sql.append("ORDER BY r.RoomNumber ASC ");
        }

        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            ps.setInt(paramIdx++, guestCount);
            ps.setObject(paramIdx++, checkOutDate);
            ps.setObject(paramIdx++, checkInDate);

            if (roomTypeId != null && roomTypeId > 0) {
                ps.setInt(paramIdx++, roomTypeId);
            }
            if (minPrice != null) {
                ps.setBigDecimal(paramIdx++, minPrice);
            }
            if (maxPrice != null) {
                ps.setBigDecimal(paramIdx++, maxPrice);
            }

            ps.setInt(paramIdx++, (index - 1) * PAGE_SIZE);
            ps.setInt(paramIdx, PAGE_SIZE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    /*
     * =========================================================
     * 6B. COUNT AVAILABLE ROOMS WITH FULL UC06 FILTERS
     * =========================================================
     */
    public int countAvailableRooms(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int guestCount,
            Integer roomTypeId,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice) {

        if (!isValidDateRange(checkInDate, checkOutDate) || guestCount <= 0) {
            return 0;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS Total "
                + "FROM Room r "
                + "JOIN RoomType rt ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.Status = 'available' AND r.NeedsCleaning=0 "
                + "AND rt.IsActive = 1 "
                + "AND rt.Capacity >= ? "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b ON b.BookingID = bd.BookingID "
                + "    WHERE bd.RoomNumber = r.RoomNumber "
                + "    AND " + RoomOccupancySql.overlaps("b", "bd") + " "
                + ") "
        );

        if (roomTypeId != null && roomTypeId > 0) {
            sql.append("AND rt.RoomTypeID = ? ");
        }
        if (minPrice != null) {
            sql.append("AND rt.PricePerNight >= ? ");
        }
        if (maxPrice != null) {
            sql.append("AND rt.PricePerNight <= ? ");
        }

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            ps.setInt(paramIdx++, guestCount);
            ps.setObject(paramIdx++, checkOutDate);
            ps.setObject(paramIdx++, checkInDate);

            if (roomTypeId != null && roomTypeId > 0) {
                ps.setInt(paramIdx++, roomTypeId);
            }
            if (minPrice != null) {
                ps.setBigDecimal(paramIdx++, minPrice);
            }
            if (maxPrice != null) {
                ps.setBigDecimal(paramIdx++, maxPrice);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 8. CHECK ROOM EXISTS
     * =========================================================
     */
    public boolean doesRoomExist(int roomNumber) {
        String sql = "SELECT 1 "
                + "FROM Room "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
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
     * Returns the generated room numbers after the entire group commits.
     * =========================================================
     */
    public List<Integer> create(String status, int roomTypeId, int quantity) {
        if (!isValidRoomStatus(status) || quantity < 1 || quantity > MAX_CREATE_QUANTITY) {
            return Collections.emptyList();
        }

        String sql = "INSERT INTO Room (RoomTypeID, Status) VALUES (?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, roomTypeId);
                ps.setString(2, status);
                List<Integer> roomNumbers = new ArrayList<>(quantity);
                for (int i = 0; i < quantity; i++) {
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Room insert did not create one row");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Missing generated room number");
                        }
                        roomNumbers.add(keys.getInt(1));
                    }
                }
                conn.commit();
                return roomNumbers;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, "Room creation failed", ex);
            return Collections.emptyList();
        }
    }

    /*
     * =========================================================
     * 10. UPDATE ROOM
     *
     * RoomNumber is primary key / identity.
     * We do not change the room number.
     * =========================================================
     */
    public int update(int roomNumber, String status, int roomTypeId) {
        if (!isValidRoomStatus(status)) {
            return 0;
        }

        String sql = "UPDATE r SET RoomTypeID = ?, Status = ? FROM Room r WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE r.RoomNumber = ? AND ((r.RoomTypeID = ? AND (r.Status = ? OR ? = 'available')) "
                + "OR " + NO_ACTIVE_BOOKING + ")";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ps.setString(2, status);
            ps.setInt(3, roomNumber);
            ps.setInt(4, roomTypeId);
            ps.setString(5, status);
            ps.setString(6, status);
            ps.setObject(7, ReportPeriod.today());
            return ps.executeUpdate() == 1 ? 1 : CHANGE_BLOCKED_BY_BOOKING;
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 11. CHECK WHETHER ROOM HAS BOOKING HISTORY
     * =========================================================
     */
    public boolean hasBookingHistory(int roomNumber) {
        String sql = "SELECT 1 "
                + "FROM BookingDetail "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
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
    public int delete(int roomNumber) {
        if (!doesRoomExist(roomNumber)) {
            return 0;
        }

        /*
         * Room already appeared in a booking.
         * Preserve historical foreign-key references.
         */
        if (hasBookingHistory(roomNumber)) {
            return setRoomOutOfService(roomNumber);
        }

        /*
         * Room has never been booked.
         * Physical delete is safe.
         */
        String sql = "DELETE FROM Room "
                + "WHERE RoomNumber = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /*
     * =========================================================
     * 13. SOFT REMOVE / DISABLE ROOM
     * =========================================================
     */
    public int setRoomOutOfService(int roomNumber) {
        String sql = "UPDATE r SET Status = 'out_of_service' FROM Room r WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE r.RoomNumber = ? AND " + NO_ACTIVE_BOOKING;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            ps.setObject(2, ReportPeriod.today());
            return ps.executeUpdate() == 1 ? 1 : CHANGE_BLOCKED_BY_BOOKING;
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
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
    private boolean isValidRoomStatus(String status) {
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
    private boolean isValidDateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return false;
        }

        return checkOutDate.isAfter(checkInDate);
    }

    /*
     * =========================================================
     * 16. MAP RESULTSET -> ROOM
     * =========================================================
     */
    private Room mapRoom(ResultSet rs) throws SQLException {
        RoomType roomType = new RoomType(
                rs.getInt("RoomTypeID"),
                rs.getString("Name"),
                rs.getString("Description"),
                rs.getBigDecimal("PricePerNight"),
                rs.getInt("Beds"),
                rs.getInt("Capacity"),
                rs.getString("Picture")
        );

        return new Room(
                rs.getInt("RoomNumber"),
                rs.getString("Status"),
                roomType
        );
    }

    /*
     * =========================================================
     * 8A. CHECK IF ROOM EXISTS AND ITS ROOM TYPE IS ACTIVE (UC07)
     *
     * Exception 2a: Room must exist and its RoomType must not be
     * deactivated by an Administrator.
     * =========================================================
     */
    public boolean isRoomViewable(int roomNumber) {
        String sql = "SELECT 1 "
                + "FROM Room r "
                + "JOIN RoomType rt ON rt.RoomTypeID = r.RoomTypeID "
                + "WHERE r.RoomNumber = ? "
                + "AND rt.IsActive = 1";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
}
