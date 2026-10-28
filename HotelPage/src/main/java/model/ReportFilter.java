/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import util.ReportPeriod;

/**
 *
 * @author Nguyen Hoang Nhat
 */
public final class ReportFilter {

    private final String view, status, method;
    private final LocalDate fromDate, toDate;
    private final Integer bookingId;
    private final int pageSize;

    public ReportFilter(String view, LocalDate from, LocalDate to, String status,
            String method, Integer bookingId, int pageSize) {
        if (!Arrays.asList("bookings", "payments", "reviews").contains(view)) {
            throw new IllegalArgumentException("Invalid report view.");
        }
        if (from != null || to != null) {
            ReportPeriod.validate(from, to);
        }
        this.view = view;
        this.fromDate = from;
        this.toDate = to;
        this.status = status == null ? "" : status;
        this.method = method == null ? "" : method;
        this.bookingId = bookingId;
        this.pageSize = pageSize;
        if (!this.status.isEmpty() && !getStatuses().contains(this.status)) {
            throw new IllegalArgumentException("Invalid status for this report.");
        }
        if (!this.method.isEmpty() && (!"payments".equals(view) || !getMethods().contains(this.method))) {
            throw new IllegalArgumentException("Invalid payment method.");
        }
        if (bookingId != null && bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID must be positive.");
        }
        if (!Arrays.asList(10, 25, 50).contains(pageSize)) {
            throw new IllegalArgumentException("Invalid page size.");
        }
    }

    public String getView() {
        return view;
    }

    public String getStatus() {
        return status;
    }

    public String getMethod() {
        return method;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<String> getStatuses() {
        if ("bookings".equals(view)) {
            return Arrays.asList("pending", "confirmed", "cancelled", "checked_in", "checked_out", "completed");
        }
        if ("payments".equals(view)) {
            return Arrays.asList("pending", "paid", "failed", "refunded");
        }
        return Arrays.asList("pending", "approved", "rejected");
    }

    public List<String> getMethods() {
        return Arrays.asList("cash", "bank_transfer", "credit_card", "e_wallet");
    }
}
