package util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class HotelStayHours {

    public static final LocalTime CHECK_IN = LocalTime.parse(System.getProperty("hotel.stay.checkIn", "14:00"));
    public static final LocalTime CHECK_OUT = LocalTime.parse(System.getProperty("hotel.stay.checkOut", "12:00"));
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("HH:mm");

    private HotelStayHours() {
    }

    public static String checkInLabel() {
        return CHECK_IN.format(DISPLAY);
    }

    public static String checkOutLabel() {
        return CHECK_OUT.format(DISPLAY);
    }
}
