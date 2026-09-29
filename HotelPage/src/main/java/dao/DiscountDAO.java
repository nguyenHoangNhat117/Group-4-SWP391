/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Discount;

/**
 * DiscountDAO provides data access methods for performing CRUD operations
 * on the Discount table in the database.
 *
 * Author: Đặng Hoàng Vũ
 */
public class DiscountDAO extends DBContext {

    /**
     * Retrieves all discount records from the database.
     *
     * @return a list of all Discount objects.
     */
    public List<Discount> getAll() {
        List<Discount> discounts = new ArrayList<>();
        String query = "SELECT * FROM Discount;";

        try {
            PreparedStatement pstatement = this.getConnection().prepareStatement(query);
            ResultSet rs = pstatement.executeQuery();

            // Iterate through each record and convert it to a Discount object
            while (rs.next()) {
                int id = rs.getInt("DiscountID");
                String code = rs.getString("Code");
                int quantity = rs.getInt("Quantity");
                BigDecimal sale = rs.getBigDecimal("SaleOff");

                Discount discount = new Discount(id, code, quantity, sale);
                discounts.add(discount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return discounts;
    }

    /**
     * Inserts a new discount record into the database.
     *
     * @param code the discount code
     * @param quantity the number of times this discount can be used
     * @param saleOff the discount amount (as a BigDecimal)
     */
    public void createDiscount(String code, int quantity, BigDecimal saleOff) {
        String sql = "INSERT INTO Discount (Code, Quantity, SaleOff) VALUES (?, ?, ?)";
        try {
            executeQuery(sql, new Object[]{code, quantity, saleOff});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing discount record.
     *
     * @param id the ID of the discount to update
     * @param code updated discount code
     * @param quantity updated quantity
     * @param saleOff updated sale value
     */
    public void updateDiscount(int id, String code, int quantity, BigDecimal saleOff) {
        String sql = "UPDATE Discount SET Code = ?, Quantity = ?, SaleOff = ? WHERE DiscountID = ?";
        try {
            executeQuery(sql, new Object[]{code, quantity, saleOff, id});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a discount record based on the discount ID.
     *
     * @param id the ID of the discount to delete
     */
    public void deleteDiscount(int id) {
        String sql = "DELETE FROM Discount WHERE DiscountID = ?";
        try {
            executeQuery(sql, new Object[]{id});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a Discount object by its ID.
     *
     * @param id the ID of the discount to retrieve
     * @return the Discount object if found, otherwise null
     */
    public Discount getDiscountById(int id) {
        String sql = "SELECT * FROM Discount WHERE DiscountID = ?";

        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{id});
            if (rs.next()) {
                Discount d = new Discount();
                d.setId(rs.getInt("DiscountID"));
                d.setCode(rs.getString("Code"));
                d.setQuantity(rs.getInt("Quantity"));
                d.setSaleOff(rs.getBigDecimal("SaleOff"));
                return d;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Discount not found
    }

    /**
     * Retrieves the ID of a discount based on its code.
     *
     * @param code the discount code to search for
     * @return the DiscountID if found, otherwise null
     */
    public Integer getDiscountIDByCode(String code) {
        String sql = "SELECT DiscountID FROM Discount WHERE code = ?";

        try {
            ResultSet rs = executeSelectionQuery(sql, new Object[]{code});
            if (rs.next()) {
                return rs.getInt("DiscountID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Code not found
    }

    /**
     * Decreases the quantity of a discount by 1 if the quantity is greater than 0.
     *
     * @param discountID the ID of the discount
     * @return true if the quantity was successfully decreased, false otherwise
     */
    public boolean decreaseDiscountQuantity(int discountID) {
        String sql = "UPDATE Discount SET Quantity = Quantity - 1 WHERE DiscountID = ? AND Quantity > 0";

        try {
            int rows = executeQuery(sql, new Object[]{discountID});
            return rows > 0; // Return true if an update occurred
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
