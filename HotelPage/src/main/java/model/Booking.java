package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking {

    private int id;
    private Customer customer;
    private LocalDateTime bookingDate;
    private String status;
    private BigDecimal totalPrice;
    private String specialRequest;
    private LocalDateTime cancellationDate;
    private String cancellationReason;

    // Một Booking có thể chứa một hoặc nhiều BookingDetail
    private List<BookingDetail> details = new ArrayList<>();

    public Booking() {
    }

    public Booking(int id, Customer customer, LocalDateTime bookingDate,
            String status, BigDecimal totalPrice,
            String specialRequest,
            LocalDateTime cancellationDate,
            String cancellationReason) {

        this.id = id;
        this.customer = customer;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.specialRequest = specialRequest;
        this.cancellationDate = cancellationDate;
        this.cancellationReason = cancellationReason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }

    public void setSpecialRequest(String specialRequest) {
        this.specialRequest = specialRequest;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public List<BookingDetail> getDetails() {
        return details;
    }

    public void setDetails(List<BookingDetail> details) {
        this.details = details;
    }
}