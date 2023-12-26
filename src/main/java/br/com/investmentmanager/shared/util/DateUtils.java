package br.com.investmentmanager.shared.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UtilityClass
public final class DateUtils {

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
