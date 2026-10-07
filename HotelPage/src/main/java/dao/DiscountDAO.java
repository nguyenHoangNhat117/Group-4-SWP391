package dao;

import db.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import model.Discount;

public class DiscountDAO extends DBContext {

    /*
     * =========================================================
     * GET ALL PROMOTIONS
     * =========================================================
     */
    public List<Discount> getAll() {

        List<Discount> discounts =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "DiscountID, "
                + "Code, "
                + "Quantity, "
                + "SaleOff, "
                + "StartDate, "
                + "EndDate, "
                + "MinimumAmount, "
                + "MaximumDiscount, "
                + "IsActive "
                + "FROM Discount "
                + "ORDER BY DiscountID DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                discounts.add(
                        mapDiscount(rs)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return discounts;
    }

    /*
     * =========================================================
     * CREATE
     *
     * Old signature preserved.
     * =========================================================
     */
    public void createDiscount(
            String code,
            int quantity,
            BigDecimal saleOff) {

        createDiscount(
                code,
                quantity,
                saleOff,
                null,
                null,
                null,
                null,
                true
        );
    }

    /*
     * Full version matching new HotelDB.
     */
    public boolean createDiscount(
            String code,
            int quantity,
            BigDecimal saleOff,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minimumAmount,
            BigDecimal maximumDiscount,
            boolean active) {

        String sql =
                "INSERT INTO Discount "
                + "(Code, Quantity, SaleOff, "
                + "StartDate, EndDate, "
                + "MinimumAmount, MaximumDiscount, "
                + "IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setInt(2, quantity);
            ps.setBigDecimal(3, saleOff);
            ps.setObject(4, startDate);
            ps.setObject(5, endDate);
            ps.setBigDecimal(6, minimumAmount);
            ps.setBigDecimal(7, maximumDiscount);
            ps.setBoolean(8, active);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * UPDATE
     *
     * Old method preserved.
     * =========================================================
     */
    public void updateDiscount(
            int id,
            String code,
            int quantity,
            BigDecimal saleOff) {

        Discount old =
                getDiscountById(id);

        if (old == null) {
            return;
        }

        updateDiscount(
                id,
                code,
                quantity,
                saleOff,
                old.getStartDate(),
                old.getEndDate(),
                old.getMinimumAmount(),
                old.getMaximumDiscount(),
                old.isActive()
        );
    }

    public boolean updateDiscount(
            int id,
            String code,
            int quantity,
            BigDecimal saleOff,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minimumAmount,
            BigDecimal maximumDiscount,
            boolean active) {

        String sql =
                "UPDATE Discount "
                + "SET "
                + "Code = ?, "
                + "Quantity = ?, "
                + "SaleOff = ?, "
                + "StartDate = ?, "
                + "EndDate = ?, "
                + "MinimumAmount = ?, "
                + "MaximumDiscount = ?, "
                + "IsActive = ? "
                + "WHERE DiscountID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setInt(2, quantity);
            ps.setBigDecimal(3, saleOff);
            ps.setObject(4, startDate);
            ps.setObject(5, endDate);
            ps.setBigDecimal(6, minimumAmount);
            ps.setBigDecimal(7, maximumDiscount);
            ps.setBoolean(8, active);
            ps.setInt(9, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * DELETE
     *
     * Không DELETE vật lý nữa.
     *
     * Payment có FK tới Discount nên promotion đã được sử dụng
     * phải được giữ để bảo toàn lịch sử.
     * =========================================================
     */
    public void deleteDiscount(int id) {

        deactivateDiscount(id);
    }

    public boolean deactivateDiscount(int id) {

        String sql =
                "UPDATE Discount "
                + "SET IsActive = 0 "
                + "WHERE DiscountID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * GET BY ID
     * =========================================================
     */
    public Discount getDiscountById(int id) {

        String sql =
                "SELECT "
                + "DiscountID, "
                + "Code, "
                + "Quantity, "
                + "SaleOff, "
                + "StartDate, "
                + "EndDate, "
                + "MinimumAmount, "
                + "MaximumDiscount, "
                + "IsActive "
                + "FROM Discount "
                + "WHERE DiscountID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {
                    return mapDiscount(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * =========================================================
     * GET ID BY CODE
     * Kept for compatibility.
     * =========================================================
     */
    public Integer getDiscountIDByCode(
            String code) {

        if (code == null
                || code.trim().isEmpty()) {

            return null;
        }

        String sql =
                "SELECT DiscountID "
                + "FROM Discount "
                + "WHERE Code = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    code.trim()
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(
                            "DiscountID"
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * =========================================================
     * VALIDATE PROMOTION
     *
     * RDS:
     * - active
     * - correct date range
     * - quantity > 0
     * - minimum amount satisfied
     * =========================================================
     */
    public Discount getValidDiscount(
            String code,
            BigDecimal bookingAmount) {

        if (code == null
                || code.trim().isEmpty()
                || bookingAmount == null) {

            return null;
        }

        String sql =
                "SELECT "
                + "DiscountID, "
                + "Code, "
                + "Quantity, "
                + "SaleOff, "
                + "StartDate, "
                + "EndDate, "
                + "MinimumAmount, "
                + "MaximumDiscount, "
                + "IsActive "
                + "FROM Discount "
                + "WHERE Code = ? "
                + "AND IsActive = 1 "
                + "AND Quantity > 0 "
                + "AND (StartDate IS NULL "
                + "     OR StartDate <= "
                + "        CAST(GETDATE() AS DATE)) "
                + "AND (EndDate IS NULL "
                + "     OR EndDate >= "
                + "        CAST(GETDATE() AS DATE)) "
                + "AND (MinimumAmount IS NULL "
                + "     OR ? >= MinimumAmount)";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    code.trim()
            );

            ps.setBigDecimal(
                    2,
                    bookingAmount
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {
                    return mapDiscount(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * =========================================================
     * PUBLIC ACTIVE PROMOTIONS
     * UC23
     * =========================================================
     */
    public List<Discount> getActivePromotions() {

        List<Discount> list =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "DiscountID, "
                + "Code, "
                + "Quantity, "
                + "SaleOff, "
                + "StartDate, "
                + "EndDate, "
                + "MinimumAmount, "
                + "MaximumDiscount, "
                + "IsActive "
                + "FROM Discount "
                + "WHERE IsActive = 1 "
                + "AND Quantity > 0 "
                + "AND (StartDate IS NULL "
                + "     OR StartDate <= "
                + "        CAST(GETDATE() AS DATE)) "
                + "AND (EndDate IS NULL "
                + "     OR EndDate >= "
                + "        CAST(GETDATE() AS DATE)) "
                + "ORDER BY SaleOff DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                list.add(
                        mapDiscount(rs)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /*
     * =========================================================
     * DECREASE QUANTITY
     *
     * PaymentDAO sẽ thực hiện việc này trong cùng transaction.
     *
     * Method này vẫn giữ cho compatibility.
     * =========================================================
     */
    public boolean decreaseDiscountQuantity(
            int discountID) {

        String sql =
                "UPDATE Discount "
                + "SET Quantity = Quantity - 1 "
                + "WHERE DiscountID = ? "
                + "AND Quantity > 0";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    discountID
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * RESULTSET -> DISCOUNT
     * =========================================================
     */
    private Discount mapDiscount(
            ResultSet rs)
            throws SQLException {

        Discount d =
                new Discount();

        d.setId(
                rs.getInt("DiscountID")
        );

        d.setCode(
                rs.getString("Code")
        );

        d.setQuantity(
                rs.getInt("Quantity")
        );

        d.setSaleOff(
                rs.getBigDecimal("SaleOff")
        );

        d.setStartDate(
                rs.getObject(
                        "StartDate",
                        LocalDate.class
                )
        );

        d.setEndDate(
                rs.getObject(
                        "EndDate",
                        LocalDate.class
                )
        );

        d.setMinimumAmount(
                rs.getBigDecimal(
                        "MinimumAmount"
                )
        );

        d.setMaximumDiscount(
                rs.getBigDecimal(
                        "MaximumDiscount"
                )
        );

        d.setActive(
                rs.getBoolean(
                        "IsActive"
                )
        );

        return d;
    }
}