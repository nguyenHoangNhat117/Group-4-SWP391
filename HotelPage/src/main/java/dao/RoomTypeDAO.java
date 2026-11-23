package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomType;
import util.ReportPeriod;
import util.RoomOccupancySql;

public class RoomTypeDAO extends DBContext {

    private static final String ADMIN_TYPES = "SELECT rt.*, "
            + "(SELECT COUNT(*) FROM Room WHERE RoomTypeID=rt.RoomTypeID) AS RoomCount, "
            + "usage.BookingCount,usage.ActiveBookingCount FROM RoomType rt OUTER APPLY ("
            + "SELECT COUNT(DISTINCT b.BookingID) AS BookingCount,"
            + "COUNT(DISTINCT CASE WHEN b.Status='checked_in' OR (b.Status IN ('pending','confirmed') AND "
            + RoomOccupancySql.active("b") + " AND d.CheckOutDate>?) THEN b.BookingID END) AS ActiveBookingCount "
            + "FROM BookingDetail d JOIN Booking b ON b.BookingID=d.BookingID "
            + "LEFT JOIN Room r ON r.RoomNumber=d.RoomNumber "
            + "WHERE COALESCE(d.RoomTypeID,r.RoomTypeID)=rt.RoomTypeID) usage";

    public List<RoomType> getAll() {
        return getAll(false);
    }

    public List<RoomType> getAllActive() {
        return getAll(true);
    }

    private List<RoomType> getAll(boolean activeOnly) {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM RoomType" + (activeOnly ? " WHERE IsActive=1" : "")
                + " ORDER BY PricePerNight,RoomTypeID";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rows = ps.executeQuery()) {
            while (rows.next()) {
                roomTypes.add(mapRoomType(rows));
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return roomTypes;
    }

    public int getTotalPages() throws SQLException {
        String sql = "SELECT CEILING(COUNT(*) * 1.0 / 5) FROM RoomType";
        try (Connection conn = getConnection(); Statement statement = conn.createStatement();
                ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getInt(1);
        }
    }

    public List<RoomType> getPage(int index) throws SQLException {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = ADMIN_TYPES + " ORDER BY rt.RoomTypeID OFFSET ? ROWS FETCH NEXT 5 ROWS ONLY";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, ReportPeriod.today());
            ps.setLong(2, (index - 1L) * 5);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    roomTypes.add(mapAdminRoomType(rows));
                }
            }
        }
        return roomTypes;
    }

    public RoomType getAdminRoomType(int id) throws SQLException {
        String sql = ADMIN_TYPES + " WHERE rt.RoomTypeID=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, ReportPeriod.today());
            ps.setInt(2, id);
            try (ResultSet rows = ps.executeQuery()) {
                return rows.next() ? mapAdminRoomType(rows) : null;
            }
        }
    }

    public boolean setActive(int id, boolean active) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE RoomType SET IsActive=? WHERE RoomTypeID=?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    public enum SaveResult { SAVED, DUPLICATE_NAME, INVALID_AMENITY, FAILED }

    public SaveResult saveRoomType(RoomType roomType, Set<Integer> amenityIds) throws SQLException {
        int id = roomType.getId();
        boolean creating = id == 0;
        String sql = creating
                ? "INSERT INTO RoomType(Name,Description,PricePerNight,Beds,Capacity,MaxChildren,Picture) VALUES(?,?,?,?,?,?,?)"
                : "UPDATE RoomType SET Name=?,Description=?,PricePerNight=?,Beds=?,Capacity=?,MaxChildren=?,"
                + "Picture=COALESCE(?,Picture) WHERE RoomTypeID=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement save = conn.prepareStatement(sql,
                    creating ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
                if (roomTypeNameExists(conn, roomType.getName(), id)) {
                    conn.rollback();
                    return SaveResult.DUPLICATE_NAME;
                }
                save.setNString(1, roomType.getName());
                save.setNString(2, roomType.getDescription());
                save.setBigDecimal(3, roomType.getPricePerNight());
                save.setInt(4, roomType.getBeds());
                save.setInt(5, roomType.getCapacity());
                save.setInt(6, roomType.getMaxChildren());
                save.setNString(7, roomType.getPicture());
                if (!creating) {
                    save.setInt(8, id);
                }
                if (save.executeUpdate() != 1) {
                    conn.rollback();
                    return SaveResult.FAILED;
                }
                if (creating) {
                    try (ResultSet keys = save.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Missing generated room type ID");
                        }
                        id = keys.getInt(1);
                    }
                }
                if (!AmenityDAO.canSelectAmenities(conn, id, amenityIds)) {
                    conn.rollback();
                    return SaveResult.INVALID_AMENITY;
                }
                AmenityDAO.replaceRoomTypeAmenities(conn, id, amenityIds);
                conn.commit();
                return SaveResult.SAVED;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public enum DeleteResult {
        DELETED, IN_USE, NOT_FOUND, FAILED
    }

    public DeleteResult deleteRoomType(int id) {
        String lockSql = "SELECT RoomTypeID FROM RoomType WITH (XLOCK, HOLDLOCK) WHERE RoomTypeID=?";
        String deleteSql = "DELETE FROM RoomType WHERE RoomTypeID=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement lock = conn.prepareStatement(lockSql);
                    PreparedStatement delete = conn.prepareStatement(deleteSql)) {
                lock.setInt(1, id);
                try (ResultSet roomType = lock.executeQuery()) {
                    if (!roomType.next()) {
                        conn.rollback();
                        return DeleteResult.NOT_FOUND;
                    }
                }
                AmenityDAO.replaceRoomTypeAmenities(conn, id, Collections.emptySet());
                delete.setInt(1, id);
                try {
                    delete.executeUpdate();
                } catch (SQLException ex) {
                    // SQL Server rejects deletion when a Room still references this type.
                    if (ex.getErrorCode() == 547) {
                        conn.rollback();
                        return DeleteResult.IN_USE;
                    }
                    throw ex;
                }
                conn.commit();
                return DeleteResult.DELETED;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
            return DeleteResult.FAILED;
        }
    }

    public RoomType getRoomTypeById(int id) {
        return getRoomTypeById(id, false);
    }

    public RoomType getActiveRoomTypeById(int id) {
        return getRoomTypeById(id, true);
    }

    private RoomType getRoomTypeById(int id, boolean activeOnly) {
        String sql = "SELECT * FROM RoomType WHERE RoomTypeID=?" + (activeOnly ? " AND IsActive=1" : "");
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rows = ps.executeQuery()) {
                return rows.next() ? mapRoomType(rows) : null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static boolean roomTypeNameExists(Connection conn, String name, int id) throws SQLException {
        String sql = "SELECT 1 FROM RoomType WITH (UPDLOCK,HOLDLOCK) WHERE Name=? AND RoomTypeID<>?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, name);
            ps.setInt(2, id);
            try (ResultSet rows = ps.executeQuery()) {
                return rows.next();
            }
        }
    }

    private static RoomType mapRoomType(ResultSet row) throws SQLException {
        RoomType type = new RoomType(row.getInt("RoomTypeID"), row.getString("Name"), row.getString("Description"),
                row.getBigDecimal("PricePerNight"), row.getInt("Beds"), row.getInt("Capacity"), row.getString("Picture"));
        type.setMaxChildren(row.getInt("MaxChildren"));
        type.setActive(row.getBoolean("IsActive"));
        return type;
    }

    private static RoomType mapAdminRoomType(ResultSet row) throws SQLException {
        RoomType type = mapRoomType(row);
        type.setUsage(row.getInt("RoomCount"), row.getInt("BookingCount"), row.getInt("ActiveBookingCount"));
        return type;
    }
}
