package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomType;

/**
 * DAO (Data Access Object) class to handle operations related to RoomType
 * entity in the database. Author: Đặng Hoàng Vũ
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
     * Calculates the total number of pages for paginated display, assuming 5
     * items per page.
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

    /**
     * Inserts a new RoomType record into the database.
     *
     * @param roomType RoomType object containing data to be inserted
     * @return number of affected rows (should be 1 if successful)
     */
    public int createRoomType(RoomType roomType) {
        String query = "INSERT INTO RoomType (Name, Description, PricePerNight, Beds, Capacity, Picture) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Object[] params = {
                roomType.getName(),
                roomType.getDescription(),
                roomType.getPricePerNight(),
                roomType.getBeds(),
                roomType.getCapacity(),
                roomType.getPicture()
            };
            return this.executeQuery(query, params);
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Updates an existing RoomType record.
     *
     * @param id RoomType ID to update
     * @param name new name
     * @param description new description
     * @param pricePerNight new price
     * @param beds number of beds
     * @param capacity capacity
     * @param picture new image URL/path
     * @return number of affected rows
     */
    public int updateRoomType(int id, String name, String description, BigDecimal pricePerNight, int beds, int capacity, String picture) {
        try {
            String query = "UPDATE RoomType SET Name = ?, Description = ?, PricePerNight = ?, Beds = ?, Capacity = ?, Picture = ? WHERE RoomTypeID = ?";
            Object[] params = {name, description, pricePerNight, beds, capacity, picture, id};
            return this.executeQuery(query, params);
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Deletes a RoomType by its ID.
     *
     * @param id RoomType ID to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteRoomType(int id) {
        try {
            String query = "DELETE FROM RoomType WHERE RoomTypeID = ?";
            Object[] params = {id};
            int rowsAffected = this.executeQuery(query, params);
            return rowsAffected > 0;
        } catch (SQLException ex) {
            Logger.getLogger(RoomTypeDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Retrieves a RoomType by its ID.
     *
     * @param id RoomType ID
     * @return RoomType object or null if not found
     */
    public RoomType getRoomTypeById(int id) {
        RoomType roomType = null;
        try {
            String query = "SELECT RoomTypeID, Name, Description, PricePerNight, Beds, Capacity, Picture FROM RoomType WHERE RoomTypeID = ?";
            PreparedStatement statement = this.getConnection().prepareStatement(query);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int roomTypeId = rs.getInt("RoomTypeID");
                String name = rs.getString("Name");
                String description = rs.getString("Description");
                BigDecimal pricePerNight = rs.getBigDecimal("PricePerNight");
                int beds = rs.getInt("Beds");
                int capacity = rs.getInt("Capacity");
                String picture = rs.getString("Picture");

                roomType = new RoomType(roomTypeId, name, description, pricePerNight, beds, capacity, picture);
            }

            rs.close();
            statement.close();
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
