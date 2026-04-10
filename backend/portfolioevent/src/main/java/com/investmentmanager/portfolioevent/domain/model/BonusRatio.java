package com.investmentmanager.portfolioevent.domain.model;

import lombok.Getter;

@Getter
public class BonusRatio {

    private final int bonus;
    private final int base;

    private BonusRatio(int bonus, int base) {
        this.bonus = bonus;
        this.base = base;
    }

    public static BonusRatio parse(String ratio) {
        if (ratio == null || ratio.isBlank()) {
            throw new IllegalArgumentException("Proporção da bonificação é obrigatória");
        }

        String[] parts = ratio.trim().split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Proporção inválida. Use o formato A:B");
        }

        try {
            int bonus = Integer.parseInt(parts[0].trim());
            int base = Integer.parseInt(parts[1].trim());
            if (bonus <= 0 || base <= 0) {
                throw new IllegalArgumentException("Proporção deve possuir valores > 0");
            }
            return new BonusRatio(bonus, base);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Proporção inválida. Use o formato A:B", ex);
        }
    }

    public String canonical() {
        return bonus + ":" + base;
    }
}
