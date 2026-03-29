package com.investmentmanager.portfolioevent.adapter.out.persistence;

public record SubscriptionPortfolioEventMetadataDocument(String subscriptionTicker)
        implements PortfolioEventMetadataDocument {
}
