package dao;

import db.DBContext;
import model.RevenueReport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles revenue report data.
 *
 * Revenue is calculated from successful payments.
 */
public class ReportDAO extends DBContext {

    /**
     * Get total paid revenue grouped by month
     * for a selected year.
     *
     * @param year selected report year
     * @return monthly revenue report
     */
    public List<RevenueReport> getRevenueByMonth(int year) {

        List<RevenueReport> list = new ArrayList<>();

        String sql
                = "SELECT "
                + "MONTH(PaymentDate) AS Month, "
                + "SUM(Amount) AS Revenue "
                + "FROM Payment "
                + "WHERE PaymentStatus = 'paid' "
                + "AND PaymentDate IS NOT NULL "
                + "AND YEAR(PaymentDate) = ? "
                + "GROUP BY MONTH(PaymentDate) "
                + "ORDER BY MONTH(PaymentDate)";

        try {

            ResultSet rs = executeSelectionQuery(
                    sql,
                    new Object[]{year}
            );

            while (rs.next()) {

                String month
                        = String.valueOf(
                                rs.getInt("Month")
                        );

                double revenue
                        = rs.getDouble("Revenue");

                RevenueReport report
                        = new RevenueReport(
                                month,
                                revenue
                        );

                list.add(report);
            }

            rs.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return list;
    }


    /**
     * Get total paid revenue grouped by year.
     *
     * @return yearly revenue report
     */
    public List<RevenueReport> getRevenueByYear() {

        List<RevenueReport> list = new ArrayList<>();

        String sql
                = "SELECT "
                + "YEAR(PaymentDate) AS Year, "
                + "SUM(Amount) AS Revenue "
                + "FROM Payment "
                + "WHERE PaymentStatus = 'paid' "
                + "AND PaymentDate IS NOT NULL "
                + "GROUP BY YEAR(PaymentDate) "
                + "ORDER BY YEAR(PaymentDate)";

        try {

            ResultSet rs = executeSelectionQuery(
                    sql,
                    null
            );

            while (rs.next()) {

                String year
                        = String.valueOf(
                                rs.getInt("Year")
                        );

                double revenue
                        = rs.getDouble("Revenue");

                RevenueReport report
                        = new RevenueReport(
                                year,
                                revenue
                        );

                list.add(report);
            }

            rs.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return list;
    }


    /**
     * Get all years that contain successful
     * payment records.
     *
     * @return available report years
     */
    public List<Integer> getAvailableYears() {

        List<Integer> years
                = new ArrayList<>();

        String sql
                = "SELECT DISTINCT "
                + "YEAR(PaymentDate) AS Year "
                + "FROM Payment "
                + "WHERE PaymentStatus = 'paid' "
                + "AND PaymentDate IS NOT NULL "
                + "ORDER BY Year";

        try {

            ResultSet rs
                    = executeSelectionQuery(
                            sql,
                            null
                    );

            while (rs.next()) {

                years.add(
                        rs.getInt("Year")
                );
            }

            rs.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return years;
    }
}