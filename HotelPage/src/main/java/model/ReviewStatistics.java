/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.math.BigDecimal;
/**
 *
 * @author Nguyen Hoang Nhat
 */
public class ReviewStatistics {

    private final long totalReviews;
    private final BigDecimal averageRating;

    public ReviewStatistics(
            long totalReviews,
            BigDecimal averageRating) {

        this.totalReviews = totalReviews;
        this.averageRating = averageRating;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public boolean isHasReviews() {
        return totalReviews > 0;
    }
}