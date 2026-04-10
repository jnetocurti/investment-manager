package com.investmentmanager.portfolioevent.domain.model;

/**
 * Tipo do evento de portfólio. Descreve o fato ocorrido, sem interpretar
 * impacto na posição — essa responsabilidade é do módulo asset.
 */
public enum EventType {
    BUY,
    SELL,
    SUBSCRIPTION,
    SUBSCRIPTION_CONVERSION,
    SPLIT,
    BONUS,
    TICKER_RENAME,
    ASSET_CONVERSION
}
