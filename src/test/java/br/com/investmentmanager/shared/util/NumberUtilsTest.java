package br.com.investmentmanager.shared.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilsTest {

    @Test
    void parseBigDecimal() {
        assertNull(NumberUtils.parseBigDecimal(null));
        assertEquals(new BigDecimal("0.01"), NumberUtils.parseBigDecimal("0,01"));
        assertEquals(new BigDecimal("1000.01"), NumberUtils.parseBigDecimal("1.000,01"));
        assertEquals(new BigDecimal("1000.000001"), NumberUtils.parseBigDecimal("1.000,000001"));
    }

    @Test
    void parseBigDecimalPattern() {
        assertThrows(Exception.class, () -> NumberUtils.parseBigDecimal("", ',', '.', "#,00000"));
    }
}