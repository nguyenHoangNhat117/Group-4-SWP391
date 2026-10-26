package dao;

import db.DBContext;
import model.ReportStatistic;
import model.RevenueReport;
import model.ReviewStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves revenue and management statistics for reports and dashboards.
 */
public class ReportDAO extends DBContext {

    /*
     * Reporting period convention:
     *
     * Both fromDate and toDate are inclusive.
     *
     * Example:
     * fromDate = 2025-07-01
     * toDate   = 2025-07-31
     *
     * SQL uses:
     * date >= 2025-07-01
     * date <  2025-08-01
     */

    // =========================================================
    // 1. MONTHLY REVENUE
    // =========================================================

    /**
     * Returns paid revenue for all twelve months of the selected year.
     * Months without successful payments have zero revenue.
     */
    public List<RevenueReport> getRevenueByMonth(int year) {

        validateYear(year);

        String sql =
                "SELECT MONTH(PaymentDate) AS ReportMonth, "
                + "SUM(Amount) AS Revenue "
                + "FROM Payment "
                + "WHERE PaymentStatus = 'paid' "
                + "AND PaymentDate >= ? "
                + "AND PaymentDate < ? "
                + "GROUP BY MONTH(PaymentDate) "
                + "ORDER BY ReportMonth";

        Date from = Date.valueOf(LocalDate.of(year, 1, 1));
        Date until = Date.valueOf(LocalDate.of(year + 1, 1, 1));

        List<RevenueReport> existing = query(
                sql,
                rs -> new RevenueReport(
                        String.valueOf(rs.getInt("ReportMonth")),
                        rs.getBigDecimal("Revenue")
                ),
                from,
                until
        );

        Map<String, BigDecimal> monthlyRevenue =
                new LinkedHashMap<>();

        for (RevenueReport item : existing) {
            monthlyRevenue.put(
                    item.getTimeUnit(),
                    item.getTotalRevenue()
            );
        }

        List<RevenueReport> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            String label = String.valueOf(month);

            result.add(new RevenueReport(
                    label,
                    monthlyRevenue.getOrDefault(
                            label,
                            BigDecimal.ZERO
                    )
            ));
        }

        return result;
    }

    // =========================================================
    // 2. YEARLY REVENUE
    // =========================================================

    /**
     * Returns successful payment revenue grouped by calendar year.
     */
    public List<RevenueReport> getRevenueByYear() {

        String sql =
                "SELECT YEAR(PaymentDate) AS ReportYear, "
                + "SUM(Amount) AS Revenue "
                + "FROM Payment "
                + "WHERE PaymentStatus = 'paid' "
                + "AND PaymentDate IS NOT NULL "
                + "GROUP BY YEAR(PaymentDate) "
                + "ORDER BY ReportYear";

        return query(
                sql,
                rs -> new RevenueReport(
                        String.valueOf(rs.getInt("ReportYear")),
                        rs.getBigDecimal("Revenue")
                )
        );
    }

    // =========================================================
    // 3. AVAILABLE REPORT YEARS
    // =========================================================

    /**
     * Returns years containing successful payments, in ascending order.
     */
    public List<Integer> getAvailableYears() {

        String sql =
                "SELECT DISTINCT YEAR(PaymentDate) AS ReportYear "
                + "FROM Payment "
                + "WHERE PaymentStatus = 'paid' "
                + "AND PaymentDate IS NOT NULL "
                + "ORDER BY ReportYear";

        return query(
                sql,
                rs -> rs.getInt("ReportYear")
        );
    }

    // =========================================================
    // 4. TOTAL REVENUE WITHIN A REPORTING PERIOD
    // =========================================================

    /**
     * Calculates revenue from paid transactions within the selected period.
     * Revenue is based on payment amounts rather than booking totals.
     */
    public BigDecimal getTotalRevenue(
            LocalDate fromDate,
            LocalDate toDate) {

        Date[] dates = getPeriodBounds(fromDate, toDate);

        String sql =
                "SELECT COALESCE(SUM(Amount), 0) AS Revenue "
                + "FROM Payment "
                + "WHERE PaymentStatus = 'paid' "
                + "AND PaymentDate >= ? "
                + "AND PaymentDate < ?";

        List<BigDecimal> result = query(
                sql,
                rs -> rs.getBigDecimal("Revenue"),
                dates[0],
                dates[1]
        );

        return result.get(0);
    }

    // =========================================================
    // 5. BOOKING STATISTICS BY STATUS
    // =========================================================

    /**
     * Counts bookings by status using their creation dates.
     * All supported statuses are returned, including those with zero bookings.
     */
    public List<ReportStatistic> getBookingStatistics(
            LocalDate fromDate,
            LocalDate toDate) {

        Date[] dates = getPeriodBounds(fromDate, toDate);

        String sql =
                "SELECT Status, COUNT_BIG(*) AS TotalCount "
                + "FROM Booking "
                + "WHERE BookingDate >= ? "
                + "AND BookingDate < ? "
                + "GROUP BY Status";

        List<ReportStatistic> existing = query(
                sql,
                rs -> new ReportStatistic(
                        rs.getString("Status"),
                        rs.getLong("TotalCount"),
                        BigDecimal.ZERO
                ),
                dates[0],
                dates[1]
        );

        return completeStatuses(
                existing,
                "pending",
                "confirmed",
                "cancelled",
                "checked_in",
                "checked_out",
                "completed"
        );
    }

    // =========================================================
    // 6. CURRENT TOTAL CUSTOMER COUNT
    // =========================================================

    /**
     * Returns the current total number of registered customers.
     * This value is not restricted to the selected reporting period.
     */
    public long getCustomerCount() {

        String sql =
                "SELECT COUNT_BIG(*) AS TotalCustomers "
                + "FROM Customer";

        List<Long> result = query(
                sql,
                rs -> rs.getLong("TotalCustomers")
        );

        return result.get(0);
    }

    // =========================================================
    // 7. CURRENT ROOM STATUS STATISTICS
    // =========================================================

    /**
     * Counts rooms by their current operational status.
     * These values do not represent historical room statuses.
     */
    public List<ReportStatistic> getRoomStatusStatistics() {

        String sql =
                "SELECT Status, COUNT_BIG(*) AS TotalCount "
                + "FROM Room "
                + "GROUP BY Status";

        List<ReportStatistic> existing = query(
                sql,
                rs -> new ReportStatistic(
                        rs.getString("Status"),
                        rs.getLong("TotalCount"),
                        BigDecimal.ZERO
                )
        );

        return completeStatuses(
                existing,
                "available",
                "maintenance",
                "out_of_service"
        );
    }

    // =========================================================
    // 8. APPROVED REVIEW STATISTICS
    // =========================================================

    /**
     * Returns the total number of approved reviews and their average rating.
     * As specified in the SDS, these are overall statistics.
     */
    public ReviewStatistics getReviewStatistics() {

        String sql =
                "SELECT COUNT_BIG(*) AS TotalReviews, "
                + "AVG(CAST(Star AS DECIMAL(10, 2))) "
                + "AS AverageRating "
                + "FROM Review "
                + "WHERE ReviewStatus = 'approved'";

        List<ReviewStatistics> result = query(
                sql,
                rs -> new ReviewStatistics(
                        rs.getLong("TotalReviews"),
                        rs.getBigDecimal("AverageRating")
                )
        );

        return result.get(0);
    }

    // =========================================================
    // 9. PAYMENT STATISTICS BY STATUS
    // =========================================================

    /**
     * Counts transactions and sums their amounts by payment status.
     *
     * Filtering uses PaymentDate.
     * Transactions with a null PaymentDate are excluded.
     * Amounts for different statuses must not be combined as paid revenue.
     */
    public List<ReportStatistic> getPaymentStatistics(
            LocalDate fromDate,
            LocalDate toDate) {

        Date[] dates = getPeriodBounds(fromDate, toDate);

        String sql =
                "SELECT PaymentStatus, "
                + "COUNT_BIG(*) AS TotalCount, "
                + "COALESCE(SUM(Amount), 0) AS TotalAmount "
                + "FROM Payment "
                + "WHERE PaymentDate >= ? "
                + "AND PaymentDate < ? "
                + "GROUP BY PaymentStatus";

        List<ReportStatistic> existing = query(
                sql,
                rs -> new ReportStatistic(
                        rs.getString("PaymentStatus"),
                        rs.getLong("TotalCount"),
                        rs.getBigDecimal("TotalAmount")
                ),
                dates[0],
                dates[1]
        );

        return completeStatuses(
                existing,
                "pending",
                "paid",
                "failed",
                "refunded"
        );
    }

    // =========================================================
    // 10. ESTIMATED ROOM OCCUPANCY RATE
    // =========================================================

    /**
     * Estimates occupancy as a percentage using stored stay dates.
     *
     * Only rooms currently marked as available are included in both
     * occupied room nights and total room capacity.
     *
     * This assumes that these rooms were operational throughout the period.
     * Historical maintenance records are not available in the current schema.
     *
     * Only checked-in, checked-out, and completed bookings are included.
     * Checkout dates are excluded from occupied nights.
     *
     * Returns zero when no currently available rooms exist.
     * In that case, the dashboard should display "Not applicable".
     */
    public double getOccupancyRate(
            LocalDate fromDate,
            LocalDate toDate) {

        Date[] dates = getPeriodBounds(fromDate, toDate);

        String sql =
                "WITH Capacity AS ( "
                + "    SELECT COUNT_BIG(*) AS RoomCount "
                + "    FROM Room "
                + "    WHERE Status = 'available' "
                + "), Occupied AS ( "
                + "    SELECT COALESCE(SUM( "
                + "        CAST(DATEDIFF(DAY, "
                + "            CASE WHEN bd.CheckInDate < ? "
                + "                 THEN ? "
                + "                 ELSE bd.CheckInDate END, "
                + "            CASE WHEN bd.CheckOutDate > ? "
                + "                 THEN ? "
                + "                 ELSE bd.CheckOutDate END "
                + "        ) AS BIGINT) "
                + "    ), 0) AS RoomNights "
                + "    FROM BookingDetail bd "
                + "    JOIN Booking b ON b.BookingID = bd.BookingID "
                + "    JOIN Room r ON r.RoomNumber = bd.RoomNumber "
                + "    WHERE r.Status = 'available' "
                + "    AND b.Status IN ( "
                + "        'checked_in', 'checked_out', 'completed' "
                + "    ) "
                + "    AND bd.CheckInDate < ? "
                + "    AND bd.CheckOutDate > ? "
                + ") "
                + "SELECT c.RoomCount, o.RoomNights "
                + "FROM Capacity c CROSS JOIN Occupied o";

        List<long[]> result = query(
                sql,
                rs -> new long[]{
                    rs.getLong("RoomCount"),
                    rs.getLong("RoomNights")
                },
                dates[0],
                dates[0],
                dates[1],
                dates[1],
                dates[1],
                dates[0]
        );

        long roomCount = result.get(0)[0];
        long occupiedRoomNights = result.get(0)[1];

        if (roomCount == 0) {
            return 0.0;
        }

        long days = ChronoUnit.DAYS.between(
                fromDate,
                toDate.plusDays(1)
        );

        BigDecimal capacity = BigDecimal.valueOf(roomCount)
                .multiply(BigDecimal.valueOf(days));

        return BigDecimal.valueOf(occupiedRoomNights)
                .multiply(BigDecimal.valueOf(100))
                .divide(capacity, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    /**
     * Validates the year and reserves room for the exclusive upper bound.
     */
    private void validateYear(int year) {

        if (year < 1 || year > 9998) {
            throw new IllegalArgumentException(
                    "Report year must be between 1 and 9998."
            );
        }
    }

    /**
     * Converts an inclusive date range into SQL query boundaries.
     * The returned end boundary is exclusive.
     */
    private Date[] getPeriodBounds(
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required."
            );
        }

        validateYear(fromDate.getYear());
        validateYear(toDate.getYear());

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date."
            );
        }

        return new Date[]{
            Date.valueOf(fromDate),
            Date.valueOf(toDate.plusDays(1))
        };
    }

    /**
     * Adds zero-valued entries for statuses without matching records.
     * Results follow the order of the supplied status names.
     */
    private List<ReportStatistic> completeStatuses(
            List<ReportStatistic> existing,
            String... statuses) {

        Map<String, ReportStatistic> indexed =
                new LinkedHashMap<>();

        for (ReportStatistic item : existing) {
            indexed.put(item.getStatus(), item);
        }

        List<ReportStatistic> result = new ArrayList<>();

        for (String status : statuses) {

            ReportStatistic item = indexed.get(status);

            if (item == null) {
                item = new ReportStatistic(
                        status,
                        0,
                        BigDecimal.ZERO
                );
            }

            result.add(item);
        }

        return result;
    }

    /**
     * Executes a parameterized query and maps its result rows.
     * JDBC resources are closed automatically.
     * SQL failures are propagated for the servlet to handle.
     */
    private <T> List<T> query(
            String sql,
            RowMapper<T> mapper,
            Object... parameters) {

        List<T> result = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }

        } catch (SQLException ex) {

            throw new IllegalStateException(
                    "Unable to load report data.",
                    ex
            );
        }

        return result;
    }

    /**
     * Maps the current result-set row to a model or value.
     */
    @FunctionalInterface
    private interface RowMapper<T> {

        T map(ResultSet rs) throws SQLException;
    }
}