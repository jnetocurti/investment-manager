package com.investmentmanager.portfolioevent.domain.model;

/**
 * Tipo do evento de portfólio. Descreve o fato ocorrido, sem interpretar
 * impacto na posição — essa responsabilidade é do módulo asset.
 */
public enum EventType {
    BUY,
    SELL
    // Futuros: DIVIDEND, SUBSCRIPTION, SUBSCRIPTION_CONVERSION,
    //          SPLIT, REVERSE_SPLIT, BONUS_SHARE, RIGHTS_ISSUE
}
