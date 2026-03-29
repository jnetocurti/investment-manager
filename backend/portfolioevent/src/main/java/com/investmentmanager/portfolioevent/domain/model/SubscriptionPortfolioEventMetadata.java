package com.investmentmanager.portfolioevent.domain.model;

public record SubscriptionPortfolioEventMetadata(String subscriptionTicker)
        implements PortfolioEventMetadata {

    public SubscriptionPortfolioEventMetadata {
        if (subscriptionTicker == null || subscriptionTicker.isBlank()) {
            throw new IllegalArgumentException("Ticker da subscrição é obrigatório");
        }
    }
}
