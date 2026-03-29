package com.investmentmanager.portfolioevent.adapter.out.persistence;

public record SubscriptionConversionPortfolioEventMetadataDocument(String subscriptionTicker)
        implements PortfolioEventMetadataDocument {
}
