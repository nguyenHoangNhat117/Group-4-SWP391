/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.ReportPeriod;
/**
 *
 * @author Nguyen Hoang Nhat
 */
public final class OccupancyEstimate {
    public static final class Stay {
        public final int roomNumber;
        public final LocalDate checkIn, checkOut;
        public Stay(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
            this.roomNumber = roomNumber; this.checkIn = checkIn; this.checkOut = checkOut;
        }
    }
    private final long roomCount, roomNights, capacityNights;
    private final LocalDate throughDate;
    private OccupancyEstimate(long rooms, long nights, long capacity, LocalDate through) {
        roomCount=rooms; roomNights=nights; capacityNights=capacity; throughDate=through;
    }
    public long getRoomCount() { return roomCount; }
    public long getRoomNights() { return roomNights; }
    public long getCapacityNights() { return capacityNights; }
    public LocalDate getThroughDate() { return throughDate; }
    public boolean isApplicable() { return capacityNights > 0; }
    public BigDecimal getRate() {
        return !isApplicable() ? BigDecimal.ZERO : BigDecimal.valueOf(roomNights)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(capacityNights), 2, RoundingMode.HALF_UP);
    }
    public static OccupancyEstimate calculate(long rooms, List<Stay> stays,
            LocalDate from, LocalDate to, LocalDate today) {
        ReportPeriod.validate(from, to);
        if (rooms < 0) throw new IllegalArgumentException("Invalid room count.");
        LocalDate until = to.plusDays(1).isBefore(today) ? to.plusDays(1) : today;
        if (!from.isBefore(until)) return new OccupancyEstimate(rooms, 0, 0, null);
        long capacity = Math.multiplyExact(rooms, ChronoUnit.DAYS.between(from, until));
        Map<Integer, List<Stay>> grouped = new HashMap<>();
        for (Stay stay : stays) {
            LocalDate start = stay.checkIn.isBefore(from) ? from : stay.checkIn;
            LocalDate end = stay.checkOut.isAfter(until) ? until : stay.checkOut;
            if (start.isBefore(end)) grouped.computeIfAbsent(stay.roomNumber, k -> new ArrayList<>())
                    .add(new Stay(stay.roomNumber, start, end));
        }
        long nights = 0;
        // Union overlapping intervals: one physical room contributes once per night.
        for (List<Stay> room : grouped.values()) {
            room.sort(Comparator.comparing(s -> s.checkIn));
            LocalDate start = room.get(0).checkIn, end = room.get(0).checkOut;
            for (int i=1; i<room.size(); i++) {
                Stay stay=room.get(i);
                if (!stay.checkIn.isAfter(end)) {
                    if (stay.checkOut.isAfter(end)) end=stay.checkOut;
                } else {
                    nights += ChronoUnit.DAYS.between(start, end);
                    start=stay.checkIn; end=stay.checkOut;
                }
            }
            nights += ChronoUnit.DAYS.between(start, end);
        }
        if (nights > capacity) throw new IllegalStateException("Room nights exceed inventory capacity.");
        return new OccupancyEstimate(rooms, nights, capacity, until.minusDays(1));
    }
}
