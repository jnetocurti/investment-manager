package br.com.investmentmanager.shared.util.constants;

import lombok.NonNull;

public enum Operation {
    BUY, SALE;

    public static Operation of(@NonNull String value) {
        return switch (value) {
            case "C" -> BUY;
            case "V" -> SALE;
            default -> throw new IllegalArgumentException();
        };
    }
}
