
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;

/**
 *
 * @author Diệp Vỹ Khang
 */
/**
 * Represents a customer review for a hotel booking experience. Contains review
 * details including rating, comments, and associated booking information.
 */
public class Review {
    private int reviewID;        // Unique identifier for the review
    private Booking booking;    // Booking associated with this review
    private String comment;     // Customer's written feedback (nullable)
    private BigDecimal star;    // Numeric rating (typically 1-5 scale)

    /**
     * Default constructor creates an empty Review object. Used when
     * initializing a review before setting properties.
     */
    public Review() {
    }

    /**
     * Creates a fully initialized Review with all properties.
     *
     * @param reviewID Unique identifier for the review
     * @param booking The Booking this review references
     * @param comment Customer's written feedback (can be null or empty)
     * @param star Numeric rating value (e.g., 4.5 for four and half stars)
     */
    public Review(int reviewID, Booking booking, String comment, BigDecimal star) {
        this.reviewID = reviewID;
        this.booking = booking;
        this.comment = comment;
        this.star = star;
    }

    /**
     * Gets the unique review identifier.
     *
     * @return int value of review ID
     */
    public int getReviewID() {
        return reviewID;
    }

    /**
     * Gets the booking associated with this review.
     *
     * @return Booking object containing reservation details
     */
    public Booking getBooking() {
        return booking;
    }

    /**
     * Gets the customer's written feedback.
     *
     * @return String containing review comments (may be empty)
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the numeric rating value.
     *
     * @return BigDecimal representing star rating (1.0-5.0 scale)
     */
    public BigDecimal getStar() {
        return star;
    }

    /**
     * Sets the unique review identifier.
     *
     * @param reviewID The ID number to assign
     */
    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    /**
     * Sets the booking associated with this review.
     *
     * @param booking The Booking object to associate
     */
    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    /**
     * Sets the customer's written feedback.
     *
     * @param comment The review text to set (can be null or empty)
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the numeric rating value.
     *
     * @param star The star rating to set (should be 1.0-5.0)
     */
    public void setStar(BigDecimal star) {
        this.star = star;
    }
}
