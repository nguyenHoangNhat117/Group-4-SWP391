package dao;

import db.DBContext;
import model.RevenueReport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportDAO handles the retrieval of revenue reports from the database.
 * It allows querying revenue grouped by month and year, and available years with data.
 * 
 * Author: Trần Nguyễn
 */
public class ReportDAO extends DBContext {

    /**
     * Retrieves total revenue for each month of a specific year.
     *
     * @param year The year to filter the revenue by.
     * @return A list of RevenueReport objects, each representing revenue in a specific month.
     */
    public List<RevenueReport> getRevenueByMonth(int year) {
        List<RevenueReport> list = new ArrayList<>();

        // SQL query to group revenue by month for the given year
        String sql = "SELECT MONTH(p.PaymentDate) AS Month, SUM(b.TotalPrice) AS Revenue\n"
                + "FROM Payment p\n"
                + "JOIN Booking b ON p.BookingID = b.BookingID\n"
                + "WHERE YEAR(p.PaymentDate) = ?\n"
                + "GROUP BY MONTH(p.PaymentDate)\n"
                + "ORDER BY MONTH(p.PaymentDate)";

        try {
            // Execute the query and pass the year as a parameter
            ResultSet rs = executeSelectionQuery(sql, new Object[]{year});
            while (rs.next()) {
                // Retrieve the month and revenue from the result set
                String month = String.valueOf(rs.getInt("Month"));
                double revenue = rs.getDouble("Revenue");
                // Add a new RevenueReport to the list
                list.add(new RevenueReport(month, revenue));
            }
            rs.close();
        } catch (SQLException e) {
            // Print any SQL exceptions for debugging
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Retrieves total revenue grouped by each year available in the database.
     *
     * @return A list of RevenueReport objects, each representing revenue in a specific year.
     */
    public List<RevenueReport> getRevenueByYear() {
        List<RevenueReport> list = new ArrayList<>();

        // SQL query to group revenue by year
        String sql = "SELECT YEAR(p.PaymentDate) AS Year, SUM(b.TotalPrice) AS Revenue\n"
                + "FROM Payment p\n"
                + "JOIN Booking b ON p.BookingID = b.BookingID\n"
                + "GROUP BY YEAR(p.PaymentDate)\n"
                + "ORDER BY YEAR(p.PaymentDate)";

        try {
            ResultSet rs = executeSelectionQuery(sql, null);
            while (rs.next()) {
                String year = String.valueOf(rs.getInt("Year"));
                double revenue = rs.getDouble("Revenue");
                list.add(new RevenueReport(year, revenue));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Retrieves all distinct years for which payments exist in the database.
     *
     * @return A list of years (integers) where payment data is available.
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();

        // SQL query to select distinct years from the Payment table
        String sql = "SELECT DISTINCT YEAR(PaymentDate) AS Year FROM Payment ORDER BY Year";

        try {
            ResultSet rs = executeSelectionQuery(sql, null);
            while (rs.next()) {
                years.add(rs.getInt("Year"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return years;
    }
}
