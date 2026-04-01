package com.investmentmanager.portfolioevent.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;

@Getter
public class SplitRatio {

    private final int from;
    private final int to;

    private SplitRatio(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static SplitRatio parse(String ratio) {
        if (ratio == null || ratio.isBlank()) {
            throw new IllegalArgumentException("Proporção do split é obrigatória");
        }

        String[] parts = ratio.trim().split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Proporção inválida. Use o formato A:B");
        }

        try {
            int from = Integer.parseInt(parts[0].trim());
            int to = Integer.parseInt(parts[1].trim());
            if (from <= 0 || to <= 0) {
                throw new IllegalArgumentException("Proporção deve possuir valores > 0");
            }
            return new SplitRatio(from, to);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Proporção inválida. Use o formato A:B", ex);
        }
    }

    public BigDecimal factor() {
        return BigDecimal.valueOf(to).divide(BigDecimal.valueOf(from), MathContext.DECIMAL64);
    }

    public String canonical() {
        return from + ":" + to;
    }
}
