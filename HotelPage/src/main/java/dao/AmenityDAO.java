package dao;

import db.DBContext;
import model.Amenity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    // Cập nhật tiện ích cho 1 loại phòng (Xóa hết cái cũ -> Thêm lại các cái mới được check)
    public boolean updateAmenitiesForRoomType(int roomTypeId, String[] amenityIds) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Dùng Transaction

            // 1. Xóa toàn bộ liên kết cũ
            String deleteSql = "DELETE FROM RoomTypeAmenity WHERE RoomTypeID = ?";
            try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                psDel.setInt(1, roomTypeId);
                psDel.executeUpdate();
            }

            // 2. Insert các liên kết mới
            if (amenityIds != null && amenityIds.length > 0) {
                String insertSql = "INSERT INTO RoomTypeAmenity (RoomTypeID, AmenityID) VALUES (?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                    for (String idStr : amenityIds) {
                        psIns.setInt(1, roomTypeId);
                        psIns.setInt(2, Integer.parseInt(idStr));
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException e) { }
            }
            Logger.getLogger(AmenityDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { }
            }
        }
    }
}