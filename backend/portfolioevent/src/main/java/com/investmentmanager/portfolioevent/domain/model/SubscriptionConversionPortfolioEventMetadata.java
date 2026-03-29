package com.investmentmanager.portfolioevent.domain.model;

public record SubscriptionConversionPortfolioEventMetadata(String subscriptionTicker)
        implements PortfolioEventMetadata {

    public SubscriptionConversionPortfolioEventMetadata {
        if (subscriptionTicker == null || subscriptionTicker.isBlank()) {
            throw new IllegalArgumentException("Ticker da subscrição é obrigatório");
        }
    }
}
