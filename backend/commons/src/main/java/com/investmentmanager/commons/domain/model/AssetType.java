package com.investmentmanager.commons.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssetType {

    STOCKS_BRL("Ações", "Brasil", "BRL"),
    REAL_ESTATE_FUND_BRL("Fundo Imobiliário", "Brasil", "BRL"),
    EXCHANGE_TRADED_FUND_BRL("ETF", "Brasil", "BRL"),
    BRAZILIAN_DEPOSITARY_RECEIPT_BRL("BDR", "Brasil", "BRL");

    private final String displayName;
    private final String market;
    private final String currency;

    public String fullDisplayName() {
        return displayName + " - " + market;
    }
}
