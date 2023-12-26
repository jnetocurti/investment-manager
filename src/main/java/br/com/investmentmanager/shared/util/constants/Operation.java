package br.com.investmentmanager.shared.util.constants;

import lombok.NonNull;

public enum Operation {
    BUY;

    public static Operation of(@NonNull String value) {
        return switch (value) {
            case "B", "C" -> BUY;
            default -> throw new IllegalArgumentException();
        };
    }
}
