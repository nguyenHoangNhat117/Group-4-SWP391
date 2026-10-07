package dao;

import db.DBContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import model.Booking;
import model.Customer;
import model.Review;
import model.User;

public class ReviewDAO extends DBContext {

    /*
     * =========================================================
     * 1. ADD REVIEW
     *
     * New review always starts as pending.
     * =========================================================
     */
    public boolean addReview(
            int bookingId,
            String comment,
            BigDecimal star) {

        if (star == null
                || star.compareTo(
                        BigDecimal.ONE) < 0
                || star.compareTo(
                        new BigDecimal("5")) > 0) {

            return false;
        }

        String sql =
                "INSERT INTO Review "
                + "(BookingID, Comment, Star, ReviewStatus) "
                + "VALUES (?, ?, ?, 'pending')";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    bookingId
            );

            ps.setString(
                    2,
                    comment
            );

            ps.setBigDecimal(
                    3,
                    star
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * 2. CHECK WHETHER BOOKING WAS REVIEWED
     * =========================================================
     */
    public boolean isBookingReviewed(
            int bookingId) {

        String sql =
                "SELECT 1 "
                + "FROM Review "
                + "WHERE BookingID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    bookingId
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * 3. PUBLIC APPROVED REVIEWS FOR ROOM
     *
     * Important:
     * RoomNumber is now in BookingDetail.
     * Only approved reviews are public.
     * =========================================================
     */
    public List<Review> getReviewsByRoomNumber(
            int roomNumber) {

        List<Review> list =
                new ArrayList<>();

        String sql =
                "SELECT DISTINCT "
                + "r.ReviewID, "
                + "r.Comment, "
                + "r.Star, "
                + "r.ReviewDate, "
                + "r.ReviewStatus, "
                + "b.BookingID, "
                + "b.BookingDate, "
                + "b.Status AS BookingStatus, "
                + "b.TotalPrice, "
                + "b.SpecialRequest, "
                + "b.CancellationDate, "
                + "b.CancellationReason, "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.Password, "
                + "u.Role, "
                + "u.DisplayName "
                + "FROM Review r "
                + "JOIN Booking b "
                + "ON b.BookingID = r.BookingID "
                + "JOIN BookingDetail bd "
                + "ON bd.BookingID = b.BookingID "
                + "JOIN Customer c "
                + "ON c.CustomerID = b.CustomerID "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "WHERE bd.RoomNumber = ? "
                + "AND r.ReviewStatus = 'approved' "
                + "ORDER BY r.ReviewDate DESC, "
                + "r.ReviewID DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomNumber
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    list.add(
                            mapReview(
                                    conn,
                                    rs
                            )
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return list;
    }

    /*
     * =========================================================
     * 4. AVERAGE STAR FOR ROOM
     *
     * Only approved reviews count.
     * =========================================================
     */
    public BigDecimal getAverageStarByRoomNumber(
            int roomNumber) {

        String sql =
                "SELECT AVG(r.Star) AS AvgStar "
                + "FROM Review r "
                + "JOIN Booking b "
                + "ON b.BookingID = r.BookingID "
                + "JOIN BookingDetail bd "
                + "ON bd.BookingID = b.BookingID "
                + "WHERE bd.RoomNumber = ? "
                + "AND r.ReviewStatus = 'approved'";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    roomNumber
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    BigDecimal avg =
                            rs.getBigDecimal(
                                    "AvgStar"
                            );

                    if (avg != null) {

                        return avg.setScale(
                                1,
                                RoundingMode.HALF_UP
                        );
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return BigDecimal.ZERO
                .setScale(1);
    }

    /*
     * =========================================================
     * 5. GET ALL REVIEWS FOR ADMIN
     *
     * Admin sees:
     * pending
     * approved
     * rejected
     * =========================================================
     */
    public List<Review> getAllReviews() {

        List<Review> list =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "r.ReviewID, "
                + "r.Comment, "
                + "r.Star, "
                + "r.ReviewDate, "
                + "r.ReviewStatus, "
                + "b.BookingID, "
                + "b.BookingDate, "
                + "b.Status AS BookingStatus, "
                + "b.TotalPrice, "
                + "b.SpecialRequest, "
                + "b.CancellationDate, "
                + "b.CancellationReason, "
                + "c.CustomerID, "
                + "c.FirstName, "
                + "c.LastName, "
                + "c.Email, "
                + "c.Phone, "
                + "c.Country, "
                + "c.City, "
                + "c.Street, "
                + "c.Avatar, "
                + "u.UserID, "
                + "u.Username, "
                + "u.Password, "
                + "u.Role, "
                + "u.DisplayName "
                + "FROM Review r "
                + "JOIN Booking b "
                + "ON b.BookingID = r.BookingID "
                + "JOIN Customer c "
                + "ON c.CustomerID = b.CustomerID "
                + "JOIN [User] u "
                + "ON u.UserID = c.UserID "
                + "ORDER BY "
                + "CASE r.ReviewStatus "
                + "WHEN 'pending' THEN 1 "
                + "WHEN 'approved' THEN 2 "
                + "WHEN 'rejected' THEN 3 "
                + "ELSE 4 END, "
                + "r.ReviewDate DESC, "
                + "r.ReviewID DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                list.add(
                        mapReview(
                                conn,
                                rs
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return list;
    }

    /*
     * =========================================================
     * 6. APPROVE REVIEW
     * =========================================================
     */
    public boolean approveReview(
            int reviewId) {

        return updateReviewStatus(
                reviewId,
                "approved"
        );
    }

    /*
     * =========================================================
     * 7. REJECT REVIEW
     * =========================================================
     */
    public boolean rejectReview(
            int reviewId) {

        return updateReviewStatus(
                reviewId,
                "rejected"
        );
    }

    /*
     * =========================================================
     * 8. UPDATE REVIEW STATUS
     * =========================================================
     */
    public boolean updateReviewStatus(
            int reviewId,
            String status) {

        if (!isValidReviewStatus(
                status)) {

            return false;
        }

        String sql =
                "UPDATE Review "
                + "SET ReviewStatus = ? "
                + "WHERE ReviewID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    status
            );

            ps.setInt(
                    2,
                    reviewId
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * 9. CHECK REVIEW OWNERSHIP / ELIGIBILITY
     *
     * Customer may review only:
     * - their own booking
     * - completed booking
     * - booking containing selected room
     * - no existing review
     * =========================================================
     */
    public boolean canCustomerReview(
            int bookingId,
            int customerId,
            int roomNumber) {

        String sql =
                "SELECT 1 "
                + "FROM Booking b "
                + "JOIN BookingDetail bd "
                + "ON bd.BookingID = b.BookingID "
                + "WHERE b.BookingID = ? "
                + "AND b.CustomerID = ? "
                + "AND bd.RoomNumber = ? "
                + "AND b.Status = 'completed' "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM Review r "
                + "    WHERE r.BookingID = b.BookingID "
                + ")";

        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    bookingId
            );

            ps.setInt(
                    2,
                    customerId
            );

            ps.setInt(
                    3,
                    roomNumber
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================================================
     * 10. MAP REVIEW
     * =========================================================
     */
    private Review mapReview(
            Connection conn,
            ResultSet rs)
            throws SQLException {

        User user =
                new User(
                        rs.getInt(
                                "UserID"
                        ),
                        rs.getString(
                                "Username"
                        ),
                        rs.getString(
                                "Password"
                        ),
                        rs.getString(
                                "Role"
                        )
                );

        user.setDisplayName(
                rs.getString(
                        "DisplayName"
                )
        );

        Customer customer =
                new Customer(
                        rs.getInt(
                                "CustomerID"
                        ),
                        rs.getString(
                                "FirstName"
                        ),
                        rs.getString(
                                "LastName"
                        ),
                        rs.getString(
                                "Email"
                        ),
                        rs.getString(
                                "Phone"
                        ),
                        rs.getString(
                                "Country"
                        ),
                        rs.getString(
                                "City"
                        ),
                        rs.getString(
                                "Street"
                        ),
                        rs.getString(
                                "Avatar"
                        ),
                        user
                );

        Booking booking =
                new Booking();

        booking.setId(
                rs.getInt(
                        "BookingID"
                )
        );

        booking.setCustomer(
                customer
        );

        if (rs.getTimestamp(
                "BookingDate") != null) {

            booking.setBookingDate(
                    rs.getTimestamp(
                            "BookingDate"
                    ).toLocalDateTime()
            );
        }

        booking.setStatus(
                rs.getString(
                        "BookingStatus"
                )
        );

        booking.setTotalPrice(
                rs.getBigDecimal(
                        "TotalPrice"
                )
        );

        booking.setSpecialRequest(
                rs.getString(
                        "SpecialRequest"
                )
        );

        if (rs.getTimestamp(
                "CancellationDate") != null) {

            booking.setCancellationDate(
                    rs.getTimestamp(
                            "CancellationDate"
                    ).toLocalDateTime()
            );
        }

        booking.setCancellationReason(
                rs.getString(
                        "CancellationReason"
                )
        );

        booking.setDetails(
                getBookingDetails(
                        conn,
                        booking.getId()
                )
        );

        Review review =
                new Review();

        review.setReviewID(
                rs.getInt(
                        "ReviewID"
                )
        );

        review.setBooking(
                booking
        );

        review.setComment(
                rs.getString(
                        "Comment"
                )
        );

        review.setStar(
                rs.getBigDecimal(
                        "Star"
                )
        );

        if (rs.getTimestamp(
                "ReviewDate") != null) {

            review.setReviewDate(
                    rs.getTimestamp(
                            "ReviewDate"
                    ).toLocalDateTime()
            );
        }

        review.setReviewStatus(
                rs.getString(
                        "ReviewStatus"
                )
        );

        return review;
    }

    /*
     * Avoid opening a second connection
     * while mapping review.
     */
    private List<model.BookingDetail>
            getBookingDetails(
                    Connection conn,
                    int bookingId)
                    throws SQLException {

        List<model.BookingDetail> list =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "BookingDetailID, "
                + "BookingID, "
                + "RoomNumber, "
                + "CheckInDate, "
                + "CheckOutDate, "
                + "PricePerNight, "
                + "GuestCount, "
                + "SpecialRequest "
                + "FROM BookingDetail "
                + "WHERE BookingID = ? "
                + "ORDER BY BookingDetailID";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    bookingId
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    model.BookingDetail detail =
                            new model.BookingDetail();

                    detail.setId(
                            rs.getInt(
                                    "BookingDetailID"
                            )
                    );

                    detail.setBookingId(
                            rs.getInt(
                                    "BookingID"
                            )
                    );

                    detail.setRoomNumber(
                            rs.getInt(
                                    "RoomNumber"
                            )
                    );

                    detail.setCheckInDate(
                            rs.getObject(
                                    "CheckInDate",
                                    java.time.LocalDate.class
                            )
                    );

                    detail.setCheckOutDate(
                            rs.getObject(
                                    "CheckOutDate",
                                    java.time.LocalDate.class
                            )
                    );

                    detail.setPricePerNight(
                            rs.getBigDecimal(
                                    "PricePerNight"
                            )
                    );

                    detail.setGuestCount(
                            rs.getInt(
                                    "GuestCount"
                            )
                    );

                    detail.setSpecialRequest(
                            rs.getString(
                                    "SpecialRequest"
                            )
                    );

                    list.add(
                            detail
                    );
                }
            }
        }

        return list;
    }

    private boolean isValidReviewStatus(
            String status) {

        return "pending".equals(status)
                || "approved".equals(status)
                || "rejected".equals(status);
    }
}