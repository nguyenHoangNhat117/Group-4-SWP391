package dao;

import db.DBContext;
import model.ReportStatistic;
import model.OccupancyEstimate;
import model.RevenueReport;
import model.ReviewStatistics;
import util.ReportPeriod;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO extends DBContext {

    private static final String MONEY_COLUMNS =
            "SUM(Amount) AS Collected, "
            + "SUM(CASE WHEN PaymentStatus='refunded' THEN Amount "
            + "ELSE COALESCE(RefundedAmount,0) END) AS Refunded ";

    private static final String SUCCESSFUL =
            "PaymentStatus IN ('paid','refunded') ";

    public List<RevenueReport> getRevenueByMonth(int year) {
        validateYear(year);

        String sql =
                "SELECT MONTH(PaymentDate) AS ReportMonth, "
                + MONEY_COLUMNS
                + "FROM Payment "
                + "WHERE " + SUCCESSFUL
                + "AND PaymentDate >= ? AND PaymentDate < ? "
                + "GROUP BY MONTH(PaymentDate) "
                + "ORDER BY ReportMonth";

        List<RevenueReport> rows = query(
                sql,
                rs -> new RevenueReport(
                        String.valueOf(rs.getInt("ReportMonth")),
                        rs.getBigDecimal("Collected"),
                        rs.getBigDecimal("Refunded")
                ),
                Date.valueOf(LocalDate.of(year, 1, 1)),
                Date.valueOf(LocalDate.of(year + 1, 1, 1))
        );

        Map<String, RevenueReport> indexed = new LinkedHashMap<>();

        for (RevenueReport row : rows) {
            indexed.put(row.getTimeUnit(), row);
        }

        List<RevenueReport> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            String label = String.valueOf(month);

            result.add(indexed.getOrDefault(
                    label,
                    new RevenueReport(
                            label,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    )
            ));
        }

        return result;
    }

    public List<RevenueReport> getRevenueByYear() {
        String sql =
                "SELECT YEAR(PaymentDate) AS ReportYear, "
                + MONEY_COLUMNS
                + "FROM Payment "
                + "WHERE " + SUCCESSFUL
                + "AND PaymentDate IS NOT NULL "
                + "GROUP BY YEAR(PaymentDate) "
                + "ORDER BY ReportYear";

        return query(
                sql,
                rs -> new RevenueReport(
                        String.valueOf(rs.getInt("ReportYear")),
                        rs.getBigDecimal("Collected"),
                        rs.getBigDecimal("Refunded")
                )
        );
    }

    public List<Integer> getAvailableYears() {
        String sql =
                "SELECT DISTINCT YEAR(PaymentDate) AS ReportYear "
                + "FROM Payment "
                + "WHERE " + SUCCESSFUL
                + "AND PaymentDate IS NOT NULL "
                + "ORDER BY ReportYear";

        return query(sql, rs -> rs.getInt("ReportYear"));
    }

    public BigDecimal getTotalRevenue(
            LocalDate fromDate,
            LocalDate toDate) {

        Date[] dates = getPeriodBounds(fromDate, toDate);

        String sql =
                "SELECT COALESCE("
                + "SUM(Amount-COALESCE(RefundedAmount,0)),0) AS Revenue "
                + "FROM Payment "
                + "WHERE PaymentStatus='paid' "
                + "AND PaymentDate >= ? AND PaymentDate < ?";

        return query(
                sql,
                rs -> rs.getBigDecimal("Revenue"),
                dates[0],
                dates[1]
        ).get(0);
    }

    public List<ReportStatistic> getBookingStatistics(
            LocalDate fromDate,
            LocalDate toDate) {

        Date[] dates = getPeriodBounds(fromDate, toDate);

        String sql =
                "SELECT Status, COUNT_BIG(*) AS TotalCount "
                + "FROM Booking "
                + "WHERE BookingDate >= ? AND BookingDate < ? "
                + "GROUP BY Status";

        List<ReportStatistic> rows = query(
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
                rows,
                "pending",
                "confirmed",
                "cancelled",
                "checked_in",
                "checked_out",
                "completed"
        );
    }

    public long getCustomerCount() {
        return query(
                "SELECT COUNT_BIG(*) AS TotalCustomers FROM Customer",
                rs -> rs.getLong("TotalCustomers")
        ).get(0);
    }

    public List<ReportStatistic> getRoomStatusStatistics() {
        List<ReportStatistic> rows = query(
                "SELECT Status, COUNT_BIG(*) AS TotalCount "
                + "FROM Room GROUP BY Status",
                rs -> new ReportStatistic(
                        rs.getString("Status"),
                        rs.getLong("TotalCount"),
                        BigDecimal.ZERO
                )
        );

        return completeStatuses(
                rows,
                "available",
                "maintenance",
                "out_of_service"
        );
    }

    public ReviewStatistics getReviewStatistics() {
        String sql =
                "SELECT COUNT_BIG(*) AS TotalReviews, "
                + "AVG(CAST(Star AS DECIMAL(10,2))) AS AverageRating "
                + "FROM Review WHERE ReviewStatus='approved'";

        return query(
                sql,
                rs -> new ReviewStatistics(
                        rs.getLong("TotalReviews"),
                        rs.getBigDecimal("AverageRating")
                )
        ).get(0);
    }

    public List<ReportStatistic> getPaymentStatistics(
            LocalDate fromDate,
            LocalDate toDate) {

        Date[] dates = getPeriodBounds(fromDate, toDate);

        String sql =
                "SELECT PaymentStatus, COUNT_BIG(*) AS TotalCount, "
                + "COALESCE(SUM(Amount),0) AS TotalAmount "
                + "FROM Payment "
                + "WHERE PaymentDate >= ? AND PaymentDate < ? "
                + "GROUP BY PaymentStatus";

        List<ReportStatistic> rows = query(
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
                rows,
                "pending",
                "paid",
                "failed",
                "refunded"
        );
    }

    public OccupancyEstimate getOccupancyEstimate(
            LocalDate from,
            LocalDate to,
            LocalDate today) {

        ReportPeriod.validate(from, to);

        List<OccupancyEstimate.Stay> stays = new ArrayList<>();
        long rooms;

        LocalDate until = to.plusDays(1).isBefore(today)
                ? to.plusDays(1) : today;

        try (Connection connection = getConnection()) {
            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE
            );
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "SELECT COUNT_BIG(*) FROM Room"
                             );
                     ResultSet rs = statement.executeQuery()) {

                    rs.next();
                    rooms = rs.getLong(1);
                }

                if (from.isBefore(until)) {
                    String sql =
                            "SELECT bd.RoomNumber, "
                            + "bd.CheckInDate, bd.CheckOutDate "
                            + "FROM BookingDetail bd "
                            + "JOIN Booking b "
                            + "ON b.BookingID=bd.BookingID "
                            + "JOIN Room r "
                            + "ON r.RoomNumber=bd.RoomNumber "
                            + "WHERE b.Status IN "
                            + "('checked_in','checked_out','completed') "
                            + "AND bd.CheckInDate < ? "
                            + "AND bd.CheckOutDate > ?";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(sql)) {

                        statement.setDate(1, Date.valueOf(until));
                        statement.setDate(2, Date.valueOf(from));

                        try (ResultSet rs = statement.executeQuery()) {
                            while (rs.next()) {
                                stays.add(new OccupancyEstimate.Stay(
                                        rs.getInt(1),
                                        rs.getDate(2).toLocalDate(),
                                        rs.getDate(3).toLocalDate()
                                ));
                            }
                        }
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Unable to load occupancy estimate.",
                    ex
            );
        }

        return OccupancyEstimate.calculate(
                rooms,
                stays,
                from,
                to,
                today
        );
    }

    public double getOccupancyRate(LocalDate from, LocalDate to) {
        return getOccupancyEstimate(
                from,
                to,
                ReportPeriod.today()
        ).getRate().doubleValue();
    }

    public List<RevenueReport> getRevenueForPeriod(
            LocalDate from,
            LocalDate to) {

        ReportPeriod.validate(from, to);

        String group = ReportPeriod.grouping(from, to);

        String expression =
                "day".equals(group)
                ? "CAST(PaymentDate AS date)"
                : "month".equals(group)
                        ? "DATEFROMPARTS(YEAR(PaymentDate),MONTH(PaymentDate),1)"
                        : "DATEFROMPARTS(YEAR(PaymentDate),1,1)";

        String sql =
                "SELECT " + expression + " AS Bucket, "
                + MONEY_COLUMNS
                + "FROM Payment "
                + "WHERE " + SUCCESSFUL
                + "AND PaymentDate >= ? AND PaymentDate < ? "
                + "GROUP BY " + expression
                + " ORDER BY Bucket";

        List<RevenueReport> rows = query(
                sql,
                rs -> new RevenueReport(
                        ReportPeriod.label(
                                rs.getDate("Bucket").toLocalDate(),
                                group
                        ),
                        rs.getBigDecimal("Collected"),
                        rs.getBigDecimal("Refunded")
                ),
                Date.valueOf(from),
                Date.valueOf(to.plusDays(1))
        );

        Map<String, RevenueReport> indexed = new LinkedHashMap<>();

        for (RevenueReport row : rows) {
            indexed.put(row.getTimeUnit(), row);
        }

        List<RevenueReport> result = new ArrayList<>();

        for (LocalDate bucket = ReportPeriod.bucket(from, group);
                !bucket.isAfter(to);
                bucket = ReportPeriod.next(bucket, group)) {

            String label = ReportPeriod.label(bucket, group);

            result.add(indexed.getOrDefault(
                    label,
                    new RevenueReport(
                            label,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    )
            ));
        }

        return result;
    }

    private void validateYear(int year) {
        if (year < 1 || year > 9998) {
            throw new IllegalArgumentException(
                    "Report year must be between 1 and 9998."
            );
        }
    }

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

    private List<ReportStatistic> completeStatuses(
            List<ReportStatistic> rows,
            String... statuses) {

        Map<String, ReportStatistic> indexed = new LinkedHashMap<>();

        for (ReportStatistic row : rows) {
            indexed.put(row.getStatus(), row);
        }

        List<ReportStatistic> result = new ArrayList<>();

        for (String status : statuses) {
            result.add(indexed.getOrDefault(
                    status,
                    new ReportStatistic(
                            status,
                            0,
                            BigDecimal.ZERO
                    )
            ));
        }

        return result;
    }

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

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}