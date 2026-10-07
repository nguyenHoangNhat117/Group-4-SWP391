package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDetail {

    private int id;
    private int bookingId;
    private int roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal pricePerNight;
    private int guestCount;
    private String specialRequest;

    public BookingDetail() {
    }

    public BookingDetail(int id, int bookingId, int roomNumber,
            LocalDate checkInDate, LocalDate checkOutDate,
            BigDecimal pricePerNight, int guestCount,
            String specialRequest) {

        this.id = id;
        this.bookingId = bookingId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.pricePerNight = pricePerNight;
        this.guestCount = guestCount;
        this.specialRequest = specialRequest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }

    public void setSpecialRequest(String specialRequest) {
        this.specialRequest = specialRequest;
    }
}