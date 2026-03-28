package com.investmentmanager.portfolioevent.domain.model;

/**
 * Origem do evento de portfólio. Identifica de onde o evento foi gerado.
 */
public enum EventSource {
    TRADING_NOTE,
    SUBSCRIPTION,
    CORPORATE_ACTION
}
