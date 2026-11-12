package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomType;

/**
 * DAO (Data Access Object) class to handle operations related to RoomType entity in the database. Author: Đặng Hoàng Vũ
 */
public class RoomTypeDAO extends DBContext {

    /**
     * Retrieves all room types from the database.
     *
     * @return a list of RoomType objects
     */
    public List<RoomType> getAll() {
        List<RoomType> roomTypes = new ArrayList<>();
        try {
            String query = "SELECT RoomTypeID, Name, Description, PricePerNight, Beds, Capacity, Picture FROM RoomType";
            PreparedStatement pstatement = this.getConnection().prepareStatement(query);
            ResultSet rs = pstatement.executeQuery();

            while (rs.next()) {
                int roomTypeId = rs.getInt(1);
                String roomName = rs.getString(2);
                String roomDescription = rs.getString(3);
                BigDecimal roomPrice = rs.getBigDecimal(4);
                int beds = rs.getInt(5);
                int capacity = rs.getInt(6);
                String picture = rs.getString(7);

                RoomType roomType = new RoomType(roomTypeId, roomName, roomDescription, roomPrice, beds, capacity, picture);
                roomTypes.add(roomType);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return roomTypes;
    }

    /**
     * Calculates the total number of pages for paginated display, assuming 5 items per page.
     *
     * @return number of total pages
     */
    public int getTotalPages() {
        try {
            String query = "SELECT CEILING(COUNT(*) * 1.0 / 5) AS TotalPages FROM RoomType rt;";
            PreparedStatement pstatement = this.getConnection().prepareStatement(query);
            ResultSet rs = pstatement.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Retrieves a specific page of RoomType entries based on page index.
     *
     * @param index page number (1-based index)
     * @return a list of RoomType objects for the specified page
     */
    public List<RoomType> getPage(int index) {
        List<RoomType> roomTypes = new ArrayList<>();
        try {
            String query = "SELECT * FROM (select ROW_NUMBER() OVER (order by RoomTypeID asc) AS i, RoomTypeID, Name, Description, PricePerNight, Beds, Capacity, Picture FROM RoomType) AS Rooms WHERE i BETWEEN ? AND ?;";
            Object[] params = {(index - 1) * 5 + 1, index * 5};
            ResultSet rs = this.executeSelectionQuery(query, params);

            while (rs.next()) {
                int roomTypeId = rs.getInt(2);
                String roomName = rs.getString(3);
                String roomDescription = rs.getString(4);
                BigDecimal roomPrice = rs.getBigDecimal(5);
                int beds = rs.getInt(6);
                int capacity = rs.getInt(7);
                String picture = rs.getString(8);

                RoomType roomType = new RoomType(roomTypeId, roomName, roomDescription, roomPrice, beds, capacity, picture);
                roomTypes.add(roomType);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return roomTypes;
    }

    public boolean saveRoomType(RoomType roomType, String[] selectedAmenities) {
        Set<Integer> amenityIds = new LinkedHashSet<>();
        try {
            if (selectedAmenities != null) {
                for (String selectedAmenity : selectedAmenities) {
                    amenityIds.add(Integer.parseInt(selectedAmenity));
                }
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        int id = roomType.getId();
        boolean creating = id == 0;
        String sql = creating
                ? "INSERT INTO RoomType (Name, Description, PricePerNight, Beds, Capacity, Picture) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE RoomType SET Name=?, Description=?, PricePerNight=?, Beds=?, Capacity=?, "
                + "Picture=COALESCE(?, Picture) WHERE RoomTypeID=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement save = conn.prepareStatement(sql,
                    creating ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
                save.setString(1, roomType.getName());
                save.setString(2, roomType.getDescription());
                save.setBigDecimal(3, roomType.getPricePerNight());
                save.setInt(4, roomType.getBeds());
                save.setInt(5, roomType.getCapacity());
                save.setString(6, roomType.getPicture());
                if (!creating) {
                    save.setInt(7, id);
                }
                if (save.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
                if (creating) {
                    try (ResultSet keys = save.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Missing generated room type ID");
                        }
                        id = keys.getInt(1);
                    }
                }
                AmenityDAO.replaceRoomTypeAmenities(conn, id, amenityIds);
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
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

    /**
     * Retrieves a RoomType by its ID.
     *
     * @param id RoomType ID
     * @return RoomType object or null if not found
     */
    public RoomType getRoomTypeById(int id) {
        return getRoomTypeById(id, false);
    }

    public RoomType getActiveRoomTypeById(int id) {
        return getRoomTypeById(id, true);
    }

    private RoomType getRoomTypeById(int id, boolean activeOnly) {
        RoomType roomType = null;
        String query = "SELECT RoomTypeID, Name, Description, PricePerNight, Beds, Capacity, Picture "
                + "FROM RoomType WHERE RoomTypeID = ?" + (activeOnly ? " AND IsActive = 1" : "");
        try (java.sql.Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    roomType = new RoomType(rs.getInt("RoomTypeID"), rs.getString("Name"),
                            rs.getString("Description"), rs.getBigDecimal("PricePerNight"),
                            rs.getInt("Beds"), rs.getInt("Capacity"), rs.getString("Picture"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return roomType;
    }

    /**
     * Checks if a RoomType name already exists in the database.
     *
     * @param roomTypeName the name to check
     * @return true if exists, false otherwise
     */
    public boolean doesRoomTypeNameExist(String roomTypeName) {
        String sql = "SELECT 1 FROM RoomType WHERE Name = ?";
        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{roomTypeName});
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a RoomType ID already exists in the database.
     *
     * @param roomTypeId the ID to check
     * @return true if exists, false otherwise
     */
    public boolean doesIDExist(int roomTypeId) {
        String sql = "SELECT 1 FROM RoomType WHERE RoomTypeID = ?";
        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{roomTypeId});
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
