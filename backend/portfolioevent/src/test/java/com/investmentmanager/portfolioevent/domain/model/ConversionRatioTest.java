package com.investmentmanager.portfolioevent.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversionRatioTest {

    @Test
    void shouldParseDecimalRatio() {
        ConversionRatio ratio = ConversionRatio.parse("1:0.992479");

        assertThat(ratio.factor()).isEqualByComparingTo("0.992479");
        assertThat(ratio.canonical()).isEqualTo("1:0.992479");
    }

    @Test
    void shouldRejectInvalidFormat() {
        assertThatThrownBy(() -> ConversionRatio.parse("1/0.992479"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato A:B");
    }
}
