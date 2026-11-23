package dao;

import db.DBContext;
import model.Amenity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RoomType;

public class AmenityDAO extends DBContext {

    public List<Amenity> getAllAmenities() throws SQLException {
        return listAmenities("SELECT AmenityID,Name,IsActive FROM Amenity ORDER BY Name,AmenityID", null);
    }

    public List<Amenity> getSelectableAmenities(int roomTypeId) throws SQLException {
        return listAmenities("SELECT a.AmenityID,a.Name,a.IsActive FROM Amenity a WHERE a.IsActive=1 OR EXISTS ("
                + "SELECT 1 FROM RoomTypeAmenity rta WHERE rta.AmenityID=a.AmenityID AND rta.RoomTypeID=?) "
                + "ORDER BY a.IsActive DESC,a.Name,a.AmenityID", roomTypeId);
    }

    private List<Amenity> listAmenities(String sql, Integer roomTypeId) throws SQLException {
        List<Amenity> list = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (roomTypeId != null) {
                ps.setInt(1, roomTypeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Amenity amenity = new Amenity(rs.getInt("AmenityID"), rs.getString("Name"));
                    amenity.setActive(rs.getBoolean("IsActive"));
                    list.add(amenity);
                }
            }
        }
        return list;
    }

    public Set<Integer> getAssignedAmenityIds(int roomTypeId) throws SQLException {
        Set<Integer> ids = new LinkedHashSet<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT AmenityID FROM RoomTypeAmenity WHERE RoomTypeID=?")) {
            ps.setInt(1, roomTypeId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    ids.add(rows.getInt(1));
                }
            }
        }
        return ids;
    }

    public Map<Integer, List<RoomType>> getRoomTypesByAmenity() throws SQLException {
        Map<Integer, List<RoomType>> types = new HashMap<>();
        String sql = "SELECT rta.AmenityID,rt.RoomTypeID,rt.Name FROM RoomTypeAmenity rta "
                + "JOIN RoomType rt ON rt.RoomTypeID=rta.RoomTypeID ORDER BY rt.Name,rt.RoomTypeID";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rows = ps.executeQuery()) {
            while (rows.next()) {
                RoomType type = new RoomType();
                type.setId(rows.getInt("RoomTypeID"));
                type.setName(rows.getString("Name"));
                types.computeIfAbsent(rows.getInt("AmenityID"), id -> new ArrayList<>()).add(type);
            }
        }
        return types;
    }

    // Lấy những tiện ích của RIÊNG 1 Loại phòng (dựa vào RoomTypeID)
    public List<Amenity> getAmenitiesByRoomTypeId(int roomTypeId) {
        List<Amenity> list = new ArrayList<>();
        String sql = "SELECT a.AmenityID, a.Name "
                   + "FROM Amenity a "
                   + "JOIN RoomTypeAmenity rta ON a.AmenityID = rta.AmenityID "
                   + "WHERE rta.RoomTypeID=? AND a.IsActive=1 ORDER BY a.AmenityID";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Amenity(rs.getInt("AmenityID"), rs.getString("Name")));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AmenityDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public enum SaveResult { SAVED, DUPLICATE_NAME, NOT_FOUND }

    public SaveResult save(Amenity amenity) throws SQLException {
        int id = amenity.getAmenityID();
        String checkSql = "SELECT 1 FROM Amenity WITH (UPDLOCK,HOLDLOCK) WHERE Name=? AND AmenityID<>?";
        String saveSql = id == 0 ? "INSERT INTO Amenity(Name) VALUES(?)"
                : "UPDATE Amenity SET Name=? WHERE AmenityID=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement check = conn.prepareStatement(checkSql);
                    PreparedStatement save = conn.prepareStatement(saveSql)) {
                check.setNString(1, amenity.getName());
                check.setInt(2, id);
                try (ResultSet rows = check.executeQuery()) {
                    if (rows.next()) {
                        conn.rollback();
                        return SaveResult.DUPLICATE_NAME;
                    }
                }
                save.setNString(1, amenity.getName());
                if (id != 0) {
                    save.setInt(2, id);
                }
                if (save.executeUpdate() != 1) {
                    conn.rollback();
                    return SaveResult.NOT_FOUND;
                }
                conn.commit();
                return SaveResult.SAVED;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public boolean setActive(int id, boolean active) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE Amenity SET IsActive=? WHERE AmenityID=?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    static boolean canSelectAmenities(Connection conn, int roomTypeId, Set<Integer> selected) throws SQLException {
        if (selected.isEmpty()) {
            return true;
        }
        String sql = "SELECT a.AmenityID FROM Amenity a WITH (HOLDLOCK) WHERE a.IsActive=1 "
                + "OR EXISTS (SELECT 1 FROM RoomTypeAmenity rta WHERE rta.RoomTypeID=? AND rta.AmenityID=a.AmenityID)";
        Set<Integer> allowed = new LinkedHashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    allowed.add(rows.getInt(1));
                }
            }
        }
        return allowed.containsAll(selected);
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
