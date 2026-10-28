/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
/**
 *
 * @author Nguyen Hoang Nhat
 */
public final class ReportPeriod {
    private ReportPeriod() { }
    public static LocalDate today() {
        return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
    public static void validate(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Please select both start date and end date.");
        if (from.getYear() < 1 || to.getYear() > 9998 || from.isAfter(to))
            throw new IllegalArgumentException("Invalid reporting period.");
        if (ChronoUnit.DAYS.between(from, to) > 3660)
            throw new IllegalArgumentException("Please select a period of at most 3,661 days.");
    }
    public static String grouping(LocalDate from, LocalDate to) {
        validate(from, to);
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days <= 31) return "day";
        long months = ChronoUnit.MONTHS.between(from.withDayOfMonth(1), to.withDayOfMonth(1)) + 1;
        return months <= 24 ? "month" : "year";
    }
    public static LocalDate bucket(LocalDate date, String group) {
        if ("day".equals(group)) return date;
        if ("month".equals(group)) return date.withDayOfMonth(1);
        if ("year".equals(group)) return date.withDayOfYear(1);
        throw new IllegalArgumentException("Unknown grouping.");
    }
    public static LocalDate next(LocalDate date, String group) {
        if ("day".equals(group)) return date.plusDays(1);
        if ("month".equals(group)) return date.plusMonths(1);
        if ("year".equals(group)) return date.plusYears(1);
        throw new IllegalArgumentException("Unknown grouping.");
    }
    public static String label(LocalDate date, String group) {
        if ("day".equals(group)) return date.toString();
        if ("month".equals(group)) return String.format("%04d-%02d", date.getYear(), date.getMonthValue());
        return String.valueOf(date.getYear());
    }
}
