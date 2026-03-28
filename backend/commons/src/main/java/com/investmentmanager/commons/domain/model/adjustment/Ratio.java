package com.investmentmanager.commons.domain.model.adjustment;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;

@Getter
@Builder
public class Ratio {

    private final BigDecimal numerator;
    private final BigDecimal denominator;

    public BigDecimal toFactor() {
        if (numerator == null || denominator == null) {
            throw new IllegalArgumentException("Numerator and denominator are required");
        }
        if (numerator.compareTo(BigDecimal.ZERO) <= 0 || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ratio values must be > 0");
        }
        return numerator.divide(denominator, MathContext.DECIMAL64);
    }
}
