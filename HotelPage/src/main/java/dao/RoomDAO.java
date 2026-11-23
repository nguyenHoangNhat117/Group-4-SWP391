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

    public static final int MAX_CREATE_QUANTITY = 50;
    private static final String ADMIN_ROOM_FILTER = " WHERE (? IS NULL OR r.RoomTypeID = ?)"
            + " AND (? IS NULL OR r.RoomNumber = ?) ";
    public static final class AvailableType {

        private final RoomType type;
        private final int available;
        private final int maxChildren;
        private final int reviewCount;
        private final java.math.BigDecimal rating;
        private final Integer readyNow;

        private AvailableType(RoomType type, int available, int maxChildren, int reviewCount,
                java.math.BigDecimal rating, Integer readyNow) {
            this.type = type;
            this.available = available;
            this.maxChildren = maxChildren;
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

        public int getMaxChildren() {
            return maxChildren;
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
            return new AvailableType(type, available, maxChildren, reviewCount, rating, count);
        }
    }

    public List<AvailableType> findRoomSelectionTypes(LocalDate from, LocalDate to, int adults,
            int children, String sort) throws SQLException {
        List<AvailableType> types = findAvailableTypes(from, to, adults, null, model.RoomPriceBand.ALL, sort);
        types.removeIf(type -> type.getMaxChildren() < children);
        return types;
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
        String sql = "SELECT rt.*,reviews.ReviewCount,reviews.Rating FROM RoomType rt CROSS APPLY ("
                + "SELECT COUNT(*) AS ReviewCount, CAST(AVG(rv.Star) AS DECIMAL(3,1)) AS Rating "
                + "FROM Review rv WHERE rv.ReviewStatus='approved' AND EXISTS ("
                + "SELECT 1 FROM BookingDetail bd LEFT JOIN Room r ON r.RoomNumber=bd.RoomNumber "
                + "WHERE bd.BookingID=rv.BookingID AND COALESCE(bd.RoomTypeID,r.RoomTypeID)=rt.RoomTypeID)"
                + ") reviews WHERE " + SALE_FILTER + " ORDER BY " + order;
        List<AvailableType> types = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSaleFilter(ps, guests, typeId, band);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("RoomTypeID");
                    int available = RoomTypeInventory.load(conn, id, from, null).available(from, to);
                    if (available == 0) {
                        continue;
                    }
                    RoomType type = new RoomType(id, rs.getString("Name"),
                            rs.getString("Description"), rs.getBigDecimal("PricePerNight"),
                            rs.getInt("Beds"), rs.getInt("Capacity"), rs.getString("Picture"));
                    types.add(new AvailableType(type, available, rs.getInt("MaxChildren"),
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
                "SELECT 1 FROM RoomType WHERE RoomTypeID=? AND IsActive=1 AND Capacity>=?")) {
            ps.setInt(1, typeId);
            ps.setInt(2, guestsPerRoom);
            try (ResultSet rows = ps.executeQuery()) {
                if (!rows.next()) {
                    return 0;
                }
            }
        }
        return RoomTypeInventory.load(connection, typeId, from, null).available(from, to);
    }

    public java.util.Map<String, String> findCalendarAvailability(LocalDate from, LocalDate to,
            int guests, Integer typeId, model.RoomPriceBand band, int quantity) throws SQLException {
        java.util.Map<LocalDate, LocalDate> limits = new java.util.TreeMap<>();
        List<RoomTypeInventory> inventories = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT rt.RoomTypeID FROM RoomType rt WHERE " + SALE_FILTER)) {
            bindSaleFilter(ps, guests, typeId, band);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    inventories.add(RoomTypeInventory.load(conn, rows.getInt(1), from, null));
                }
            }
        }
        for (RoomTypeInventory inventory : inventories) {
            for (LocalDate start = from; start.isBefore(to); start = start.plusDays(1)) {
                LocalDate earliest = limits.getOrDefault(start, start);
                long low = earliest.toEpochDay();
                long high = to.toEpochDay();
                while (low < high) {
                    long day = low + (high - low + 1) / 2;
                    LocalDate end = LocalDate.ofEpochDay(day);
                    if (inventory.reserve(start, end, quantity).size() == quantity) {
                        low = day;
                    } else {
                        high = day - 1;
                    }
                }
                if (low > earliest.toEpochDay()) {
                    limits.put(start, LocalDate.ofEpochDay(low));
                }
            }
        }
        java.util.Map<String, String> availability = new java.util.LinkedHashMap<>();
        limits.forEach((start, end) -> availability.put(start.toString(), end.toString()));
        return availability;
    }

    private static final String SALE_FILTER = "rt.IsActive=1 AND rt.Capacity>=? "
            + "AND (? IS NULL OR rt.RoomTypeID=?) AND (? IS NULL OR rt.PricePerNight>=?) "
            + "AND (? IS NULL OR rt.PricePerNight<?)";

    private static void bindSaleFilter(PreparedStatement ps, int guests, Integer typeId,
            model.RoomPriceBand band) throws SQLException {
        ps.setInt(1, guests);
        ps.setObject(2, typeId, Types.INTEGER);
        ps.setObject(3, typeId, Types.INTEGER);
        ps.setBigDecimal(4, band.getMinimum());
        ps.setBigDecimal(5, band.getMinimum());
        ps.setBigDecimal(6, band.getUpperExclusive());
        ps.setBigDecimal(7, band.getUpperExclusive());
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
    public List<Integer> create(String status, int roomTypeId, int quantity)
            throws RoomLimitExceededException {
        if (!isValidRoomStatus(status) || quantity < 1 || quantity > MAX_CREATE_QUANTITY) {
            return Collections.emptyList();
        }

        String sql = "INSERT INTO Room (RoomTypeID, Status) VALUES (?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Lock Room before reading the limit, in the same order as a limit update.
                int registered = countRooms(conn, true);
                if (quantity > roomLimit(conn) - registered) {
                    throw new RoomLimitExceededException();
                }
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
            } catch (SQLException | RoomLimitExceededException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, "Room creation failed", ex);
            return Collections.emptyList();
        }
    }

    public RoomCapacity roomCapacity() throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                RoomCapacity capacity = new RoomCapacity(countRooms(conn, false), roomLimit(conn));
                conn.commit();
                return capacity;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public boolean updateRoomLimit(int limit) throws SQLException {
        if (limit < 1) {
            return false;
        }
        String sql = "UPDATE dbo.RoomInventorySettings SET MaxRooms=? WHERE SettingID=1";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (limit < countRooms(conn, true)) {
                    conn.rollback();
                    return false;
                }
                ps.setInt(1, limit);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Hotel room inventory settings are missing");
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private static int countRooms(Connection conn, boolean exclusive) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Room WITH (" + (exclusive ? "TABLOCKX" : "TABLOCK") + ", HOLDLOCK)";
        try (Statement statement = conn.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private static int roomLimit(Connection conn) throws SQLException {
        String sql = "SELECT MaxRooms FROM dbo.RoomInventorySettings WHERE SettingID=1";
        try (Statement statement = conn.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            if (!rows.next()) {
                throw new SQLException("Hotel room inventory settings are missing");
            }
            return rows.getInt(1);
        }
    }

    public static final class RoomCapacity {
        private final int registered;
        private final int limit;

        private RoomCapacity(int registered, int limit) {
            this.registered = registered;
            this.limit = limit;
        }

        public int getRegistered() {
            return registered;
        }

        public int getLimit() {
            return limit;
        }

        public int getRemaining() {
            return Math.max(0, limit - registered);
        }
    }

    public static final class RoomLimitExceededException extends Exception {
        private static final long serialVersionUID = 1L;
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
        return isValidRoomStatus(status) ? changeRoom(roomNumber, status, roomTypeId) : 0;
    }

    public int delete(int roomNumber) {
        return changeRoom(roomNumber, null, null);
    }

    public static final class InventoryConflict extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final LocalDate from;
        private final LocalDate to;

        private InventoryConflict(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }

        public LocalDate getFrom() { return from; }
        public LocalDate getTo() { return to; }
    }

    private int changeRoom(int roomNumber, String status, Integer newType) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer oldType = currentType(conn, roomNumber);
                if (oldType == null) {
                    conn.rollback();
                    return 0;
                }
                java.util.Set<Integer> types = new java.util.TreeSet<>();
                types.add(oldType);
                if (newType != null) {
                    types.add(newType);
                }
                for (int type : types) {
                    BookingDAO.lockRoomType(conn, type);
                }

                String oldStatus;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT Status FROM Room WITH(UPDLOCK,HOLDLOCK) WHERE RoomNumber=? AND RoomTypeID=?")) {
                    ps.setInt(1, roomNumber);
                    ps.setInt(2, oldType);
                    try (ResultSet rows = ps.executeQuery()) {
                        if (!rows.next()) {
                            conn.rollback();
                            return 0;
                        }
                        oldStatus = rows.getString(1);
                    }
                }

                boolean reducing = newType == null || !oldType.equals(newType)
                        || ("available".equals(oldStatus) && !"available".equals(status));
                if (reducing) {
                    LocalDate today = ReportPeriod.today();
                    checkFixedBookings(conn, roomNumber, today);
                    if ("available".equals(oldStatus)) {
                        RoomTypeInventory inventory = RoomTypeInventory.load(conn, oldType, today, roomNumber);
                        RoomTypeInventory.Stay conflict = inventory.moveHolds(conn, roomNumber);
                        if (conflict != null) {
                            throw new InventoryConflict(conflict.from, conflict.to);
                        }
                    }
                }

                if (newType == null) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Room WHERE RoomNumber=? "
                            + "AND NOT EXISTS (SELECT 1 FROM BookingDetail WHERE RoomNumber=?)")) {
                        ps.setInt(1, roomNumber);
                        ps.setInt(2, roomNumber);
                        if (ps.executeUpdate() == 1) {
                            conn.commit();
                            return 1;
                        }
                    }
                    newType = oldType;
                    status = "out_of_service";
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Room SET RoomTypeID=?,Status=? WHERE RoomNumber=?")) {
                    ps.setInt(1, newType);
                    ps.setString(2, status);
                    ps.setInt(3, roomNumber);
                    int changed = ps.executeUpdate();
                    conn.commit();
                    return changed;
                }
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, "Room inventory change failed", ex);
            return 0;
        }
    }

    private static Integer currentType(Connection conn, int roomNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT RoomTypeID FROM Room WHERE RoomNumber=?")) {
            ps.setInt(1, roomNumber);
            try (ResultSet rows = ps.executeQuery()) {
                return rows.next() ? rows.getInt(1) : null;
            }
        }
    }

    private static void checkFixedBookings(Connection conn, int roomNumber, LocalDate today) throws SQLException {
        String sql = "SELECT TOP(1) d.CheckInDate,d.CheckOutDate FROM BookingDetail d "
                + "JOIN Booking b ON b.BookingID=d.BookingID WHERE d.RoomNumber=? AND "
                + "(b.Status='checked_in' OR (d.IsProvisionalRoom=0 AND b.Status IN ('pending','confirmed') "
                + "AND d.CheckOutDate>? AND " + RoomOccupancySql.active("b") + ")) ORDER BY d.CheckInDate";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            ps.setObject(2, today);
            try (ResultSet rows = ps.executeQuery()) {
                if (rows.next()) {
                    throw new InventoryConflict(rows.getObject(1, LocalDate.class), rows.getObject(2, LocalDate.class));
                }
            }
        }
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
