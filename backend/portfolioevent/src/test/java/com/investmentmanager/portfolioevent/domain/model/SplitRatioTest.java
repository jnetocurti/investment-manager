package com.investmentmanager.portfolioevent.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplitRatioTest {

    @Test
    void shouldParseValidRatio() {
        SplitRatio ratio = SplitRatio.parse("1:10");

        assertThat(ratio.factor()).isEqualByComparingTo("10");
        assertThat(ratio.canonical()).isEqualTo("1:10");
    }

    @Test
    void shouldRejectInvalidRatio() {
        assertThatThrownBy(() -> SplitRatio.parse("1/10"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato A:B");
    }
}
