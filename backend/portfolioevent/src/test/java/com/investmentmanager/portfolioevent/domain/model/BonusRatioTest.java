package com.investmentmanager.portfolioevent.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BonusRatioTest {

    @Test
    void shouldParseValidRatio() {
        BonusRatio ratio = BonusRatio.parse("1:10");

        assertEquals(1, ratio.getBonus());
        assertEquals(10, ratio.getBase());
        assertEquals("1:10", ratio.canonical());
        assertEquals("0.1", ratio.factor().toPlainString());
    }

    @Test
    void shouldRejectInvalidRatio() {
        assertThrows(IllegalArgumentException.class, () -> BonusRatio.parse("1/10"));
    }
}
