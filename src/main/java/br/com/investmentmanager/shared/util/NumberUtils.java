package br.com.investmentmanager.shared.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

@UtilityClass
public final class NumberUtils {

    public static BigDecimal parseBigDecimal(String value) {
        return parseBigDecimal(value, ',', '.', "#,00000");
    }

    public static BigDecimal parseBigDecimal(String value, char decimalSeparator, char groupingSeparator, String pattern) {
        if (value == null) {
            return null;
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);

        try {
            return (BigDecimal) decimalFormat.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
