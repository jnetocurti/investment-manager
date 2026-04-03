package com.investmentmanager.portfolioevent.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;

@Getter
public class ConversionRatio {

    private final BigDecimal from;
    private final BigDecimal to;

    private ConversionRatio(BigDecimal from, BigDecimal to) {
        this.from = from;
        this.to = to;
    }

    public static ConversionRatio parse(String ratio) {
        if (ratio == null || ratio.isBlank()) {
            throw new IllegalArgumentException("Proporção de conversão é obrigatória");
        }

        String[] parts = ratio.trim().split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Proporção inválida. Use o formato A:B");
        }

        try {
            BigDecimal from = new BigDecimal(parts[0].trim());
            BigDecimal to = new BigDecimal(parts[1].trim());
            if (from.compareTo(BigDecimal.ZERO) <= 0 || to.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Proporção deve possuir valores > 0");
            }
            return new ConversionRatio(from, to);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Proporção inválida. Use o formato A:B", ex);
        }
    }

    public BigDecimal factor() {
        return to.divide(from, MathContext.DECIMAL64);
    }

    public String canonical() {
        return from.stripTrailingZeros().toPlainString() + ":" + to.stripTrailingZeros().toPlainString();
    }
}
