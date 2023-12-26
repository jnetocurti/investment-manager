package br.com.investmentmanager.shared.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static LocalDate parseLocalDate(String value) {
        return parseLocalDate(value, "dd/MM/yyyy");
    }

    public static LocalDate parseLocalDate(String value, String pattern) {
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
    }
}
