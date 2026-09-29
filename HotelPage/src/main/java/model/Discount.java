/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;

/**
 *
 * @author Đặng Hoàng Vũ
 */
/**
 * Represents a discount entity containing promotion details including discount
 * code, available quantity, and percentage off. This class models promotional
 * offers for the hotel booking system.
 */
public class Discount {
    private int id;             // Unique identifier for the discount
    private String code;        // Promotion code customers can use
    private int quantity;       // Number of times this discount can be applied
    private BigDecimal saleOff; // Percentage discount (e.g., 10.00 for 10%)

    /**
     * Default constructor creates an empty Discount object. Used when
     * initializing a discount before setting properties.
     */
    public Discount() {
    }

    /**
     * Creates a fully initialized Discount with all properties.
     *
     * @param id Unique identifier for the discount
     * @param code Promotion code string
     * @param quantity Available usage count for this discount
     * @param saleOff Percentage discount value (e.g., 10.00 for 10% off)
     */
    public Discount(int id, String code, int quantity, BigDecimal saleOff) {
        this.id = id;
        this.code = code;
        this.quantity = quantity;
        this.saleOff = saleOff;
    }

    /**
     * Gets the discount's unique ID.
     *
     * @return The discount ID number
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the promotion code for this discount.
     *
     * @return String containing the discount code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the remaining available uses for this discount.
     *
     * @return Integer count of remaining uses
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the percentage discount value.
     *
     * @return BigDecimal representing discount percentage (e.g., 10.00)
     */
    public BigDecimal getSaleOff() {
        return saleOff;
    }

    /**
     * Sets the discount's unique ID.
     *
     * @param id The ID number to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the promotion code for this discount.
     *
     * @param code The discount code string to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Sets the available usage count for this discount.
     *
     * @param quantity The number of remaining uses to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Sets the percentage discount value.
     *
     * @param saleOff The discount percentage to set (e.g., 10.00)
     */
    public void setSaleOff(BigDecimal saleOff) {
        this.saleOff = saleOff;
    }
}
