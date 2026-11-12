package dao;

import db.DBContext;
import model.Amenity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AmenityDAO extends DBContext {

    // Lấy TẤT CẢ tiện ích có trong hệ thống (dùng để render checkbox lúc thêm/sửa loại phòng)
    public List<Amenity> getAllAmenities() {
        List<Amenity> list = new ArrayList<>();
        String sql = "SELECT AmenityID, Name, Description FROM Amenity ORDER BY AmenityID ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Amenity(rs.getInt("AmenityID"), rs.getString("Name"), rs.getString("Description")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AmenityDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    // Lấy những tiện ích của RIÊNG 1 Loại phòng (dựa vào RoomTypeID)
    public List<Amenity> getAmenitiesByRoomTypeId(int roomTypeId) {
        List<Amenity> list = new ArrayList<>();
        String sql = "SELECT a.AmenityID, a.Name, a.Description "
                   + "FROM Amenity a "
                   + "JOIN RoomTypeAmenity rta ON a.AmenityID = rta.AmenityID "
                   + "WHERE rta.RoomTypeID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Amenity(rs.getInt("AmenityID"), rs.getString("Name"), rs.getString("Description")));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AmenityDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    static void replaceRoomTypeAmenities(Connection conn, int roomTypeId, Set<Integer> amenityIds)
            throws SQLException {
        String deleteSql = "DELETE FROM RoomTypeAmenity WHERE RoomTypeID = ?";
        try (PreparedStatement delete = conn.prepareStatement(deleteSql)) {
            delete.setInt(1, roomTypeId);
            delete.executeUpdate();
        }

        if (amenityIds.isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO RoomTypeAmenity (RoomTypeID, AmenityID) VALUES (?, ?)";
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            for (int amenityId : amenityIds) {
                insert.setInt(1, roomTypeId);
                insert.setInt(2, amenityId);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }
}
