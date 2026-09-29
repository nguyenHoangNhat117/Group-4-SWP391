/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Trần Nguyễn
 */
/**
 * Represents a revenue report for a specific time period. This class holds
 * financial data aggregated by time units (day, week, month, etc.).
 */
public class RevenueReport {
    // Time period identifier (e.g., "2023-04", "Week 15", "Q2")
    private String timeUnit;

    // Total revenue amount for the specified time period
    private double totalRevenue;

    /**
     * Constructs a RevenueReport with specified time period and revenue amount.
     *
     * @param timeUnit The time period identifier (e.g., month, quarter, year)
     * @param totalRevenue The total revenue generated in this period
     */
    public RevenueReport(String timeUnit, double totalRevenue) {
        this.timeUnit = timeUnit;
        this.totalRevenue = totalRevenue;
    }

    /**
     * Gets the time period identifier for this report.
     *
     * @return String representing the time period (e.g., "April 2023")
     */
    public String getTimeUnit() {
        return timeUnit;
    }

    /**
     * Gets the total revenue amount for this period.
     *
     * @return double value of the total revenue
     */
    public double getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * Sets the time period identifier for this report.
     *
     * @param timeUnit The time period to set (e.g., "Q1 2023")
     */
    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Sets the total revenue amount for this period.
     *
     * @param totalRevenue The revenue amount to set
     */
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
