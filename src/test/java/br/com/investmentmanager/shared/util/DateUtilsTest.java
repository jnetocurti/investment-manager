package br.com.investmentmanager.shared.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateUtilsTest {

    @Test
    void parseLocalDate() {
        assertNull(DateUtils.parseLocalDate(null));
        assertEquals(LocalDate.of(2023, 12, 26), DateUtils.parseLocalDate("26/12/2023"));
    }

    @Test
    void testParseLocalDate() {
        assertNull(DateUtils.parseLocalDate(null, "yyyy-MM-dd"));
        assertEquals(LocalDate.of(2023, 12, 26), DateUtils.parseLocalDate("2023-12-26", "yyyy-MM-dd"));
    }
}