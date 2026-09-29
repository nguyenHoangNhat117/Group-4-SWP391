/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author Đặng Hoàng Vũ
 */
/**
 * Represents a hotel room booking with customer details, room information, stay
 * dates, and pricing. Maintains all necessary booking information.
 */
public class Booking {
    private int id;                 // Unique identifier for the booking
    private Customer customer;      // Customer who made the booking
    private int roomNumber;         // Room number assigned for the stay
    private LocalDate checkInDate;  // Scheduled arrival date
    private LocalDate checkOutDate; // Scheduled departure date
    private BigDecimal totalPrice;  // Total cost for the entire stay

    /**
     * Creates an empty Booking object with no initialized values.
     */
    public Booking() {
    }

    /**
     * Creates a Booking object with all required details. The constructor
     * initializes all fields with the provided values.
     *
     * @param id The unique booking identifier (typically from database)
     * @param customer The Customer object associated with this booking
     * @param roomNumber The room number being booked
     * @param checkInDate The starting date of the reservation
     * @param checkOutDate The ending date of the reservation
     * @param totalPrice The full price for the entire booking period
     */
    public Booking(int id, Customer customer, int roomNumber, LocalDate checkInDate,
            LocalDate checkOutDate, BigDecimal totalPrice) {
        this.id = id;
        this.customer = customer;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
    }

    /**
     * Retrieves the unique identifier for this booking. The ID is typically
     * assigned by the database when the booking is created.
     *
     * @return The booking's unique ID number
     */
    public int getId() {
        return id;
    }

    /**
     * Updates the unique identifier for this booking. Should only be used when
     * syncing with database records.
     *
     * @param id The new ID number to assign to this booking
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the Customer object associated with this booking. Contains all
     * customer details including personal information and contact methods.
     *
     * @return The Customer who made this booking
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Associates a Customer with this booking. Typically used when creating new
     * bookings or updating customer information.
     *
     * @param customer The Customer to associate with this booking
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * Retrieves the room number assigned to this booking. The number
     * corresponds to the physical room in the hotel property.
     *
     * @return The booked room's number
     */
    public int getRoomNumber() {
        return roomNumber;
    }

    /**
     * Assigns a room number to this booking. Used during room assignment or
     * when changing rooms for an existing booking.
     *
     * @param roomNumber The room number to assign
     */
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    /**
     * Gets the scheduled arrival date for this booking. The date when the guest
     * is expected to check in.
     *
     * @return The check-in date
     */
    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    /**
     * Sets the arrival date for this booking. Used when creating or modifying
     * reservation dates.
     *
     * @param checkInDate The new check-in date to set
     */
    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    /**
     * Gets the scheduled departure date for this booking. The date when the
     * guest is expected to check out.
     *
     * @return The check-out date
     */
    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    /**
     * Sets the departure date for this booking. Used when creating or modifying
     * reservation dates.
     *
     * @param checkOutDate The new check-out date to set
     */
    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    /**
     * Retrieves the total price for this booking. Includes all charges for the
     * entire stay duration.
     *
     * @return The total booking price
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Updates the total price for this booking. Typically calculated based on
     * room rate and stay duration.
     *
     * @param totalPrice The new total price to set
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
