package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class OnlineStayDates {
    public static final int ADVANCE_DAYS = 365;
    private static final DateTimeFormatter SEARCH_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter LEGACY_SEARCH_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);

    private OnlineStayDates() {
    }

    public static boolean isValid(LocalDate checkIn, LocalDate checkOut, LocalDate today) {
        return checkIn != null && checkOut != null && !checkIn.isBefore(today)
                && checkOut.isAfter(checkIn) && !checkOut.isAfter(today.plusDays(ADVANCE_DAYS));
    }

    public static LocalDate parseSearchDate(String value) {
        String date = value.trim();
        return LocalDate.parse(date, date.contains("/") ? SEARCH_FORMAT : LEGACY_SEARCH_FORMAT);
    }

    public static LocalDate parseIsoDate(String value) {
        try {
            return value == null ? null : LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static String formatSearchRange(LocalDate checkIn, LocalDate checkOut) {
        return checkIn.format(SEARCH_FORMAT) + " - " + checkOut.format(SEARCH_FORMAT);
    }
}
