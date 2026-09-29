/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author Hà Thái Phát
 */
/**
 * Represents a payment transaction for a hotel booking. This class contains
 * payment details including the associated booking, payment date, and any
 * applied discount.
 */
public class Payment {
    private int id;             // Unique identifier for the payment transaction
    private Booking booking;    // The booking associated with this payment
    private LocalDate paymentDate;  // Date when payment was processed
    private Discount discount;  // Discount applied to this payment (nullable)

    /**
     * Default constructor creates an empty Payment object. Used when
     * initializing a payment before setting properties.
     */
    public Payment() {
    }

    /**
     * Creates a fully initialized Payment with all properties.
     *
     * @param id Unique identifier for the payment
     * @param booking The Booking object this payment applies to
     * @param paymentDate Date when payment was processed
     * @param discount Discount applied to this payment (can be null)
     */
    public Payment(int id, Booking booking, LocalDate paymentDate, Discount discount) {
        this.id = id;
        this.booking = booking;
        this.paymentDate = paymentDate;
        this.discount = discount;
    }

    /**
     * Gets the payment's unique ID.
     *
     * @return The payment ID number
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the booking associated with this payment.
     *
     * @return Booking object containing reservation details
     */
    public Booking getBooking() {
        return booking;
    }

    /**
     * Gets the date when payment was processed.
     *
     * @return LocalDate object representing payment date
     */
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    /**
     * Gets the discount applied to this payment.
     *
     * @return Discount object or null if no discount was applied
     */
    public Discount getDiscount() {
        return discount;
    }

    /**
     * Sets the payment's unique ID.
     *
     * @param id The ID number to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the booking associated with this payment.
     *
     * @param booking The Booking object to associate
     */
    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    /**
     * Sets the date when payment was processed.
     *
     * @param paymentDate The payment date to set
     */
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    /**
     * Sets the discount applied to this payment.
     *
     * @param discount The Discount object to apply (can be null)
     */
    public void setDiscount(Discount discount) {
        this.discount = discount;
    }
}
