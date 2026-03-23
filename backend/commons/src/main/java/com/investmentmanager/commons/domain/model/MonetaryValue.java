package com.investmentmanager.commons.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class MonetaryValue {

    private static final int CALCULATION_SCALE = 6;
    private static final int DISPLAY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BigDecimal value;

    private MonetaryValue(BigDecimal value) {
        this.value = value.setScale(CALCULATION_SCALE, ROUNDING);
    }

    public static MonetaryValue of(BigDecimal value) {
        Objects.requireNonNull(value, "Monetary value cannot be null");
        return new MonetaryValue(value);
    }

    public static MonetaryValue of(String value) {
        return of(new BigDecimal(value));
    }

    public static MonetaryValue of(double value) {
        return of(BigDecimal.valueOf(value));
    }

    public static MonetaryValue zero() {
        return new MonetaryValue(BigDecimal.ZERO);
    }

    public MonetaryValue add(MonetaryValue other) {
        return new MonetaryValue(this.value.add(other.value));
    }

    public MonetaryValue subtract(MonetaryValue other) {
        return new MonetaryValue(this.value.subtract(other.value));
    }

    public MonetaryValue multiply(BigDecimal factor) {
        return new MonetaryValue(this.value.multiply(factor));
    }

    public MonetaryValue multiply(long factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public MonetaryValue divide(MonetaryValue divisor) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new MonetaryValue(this.value.divide(divisor.value, CALCULATION_SCALE, ROUNDING));
    }

    public MonetaryValue divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new MonetaryValue(this.value.divide(divisor, CALCULATION_SCALE, ROUNDING));
    }

    public MonetaryValue abs() {
        return new MonetaryValue(this.value.abs());
    }

    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Returns the raw value with full calculation precision (6 decimal places).
     */
    public BigDecimal toBigDecimal() {
        return this.value;
    }

    /**
     * Returns the value rounded to display precision (2 decimal places).
     */
    public BigDecimal toDisplayValue() {
        return this.value.setScale(DISPLAY_SCALE, ROUNDING);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonetaryValue that = (MonetaryValue) o;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return toDisplayValue().toPlainString();
    }
}
