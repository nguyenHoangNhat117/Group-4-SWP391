package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Review {

    private int reviewID;
    private Booking booking;
    private String comment;
    private BigDecimal star;

    private LocalDateTime reviewDate;
    private String reviewStatus;

    public Review() {
    }

    /*
     * Giữ constructor cũ để các phần chưa sửa
     * không bị lỗi compile ngay.
     */
    public Review(
            int reviewID,
            Booking booking,
            String comment,
            BigDecimal star) {

        this.reviewID = reviewID;
        this.booking = booking;
        this.comment = comment;
        this.star = star;
    }

    public Review(
            int reviewID,
            Booking booking,
            String comment,
            BigDecimal star,
            LocalDateTime reviewDate,
            String reviewStatus) {

        this.reviewID = reviewID;
        this.booking = booking;
        this.comment = comment;
        this.star = star;
        this.reviewDate = reviewDate;
        this.reviewStatus = reviewStatus;
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BigDecimal getStar() {
        return star;
    }

    public void setStar(BigDecimal star) {
        this.star = star;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(
            LocalDateTime reviewDate) {

        this.reviewDate = reviewDate;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(
            String reviewStatus) {

        this.reviewStatus = reviewStatus;
    }
}