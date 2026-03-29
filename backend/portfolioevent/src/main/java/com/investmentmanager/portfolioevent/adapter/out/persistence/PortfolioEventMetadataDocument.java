package com.investmentmanager.portfolioevent.adapter.out.persistence;

public sealed interface PortfolioEventMetadataDocument
        permits SubscriptionPortfolioEventMetadataDocument, SubscriptionConversionPortfolioEventMetadataDocument {
}
