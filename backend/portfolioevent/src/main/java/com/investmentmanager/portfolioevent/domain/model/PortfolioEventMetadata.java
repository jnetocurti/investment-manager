package com.investmentmanager.portfolioevent.domain.model;

public sealed interface PortfolioEventMetadata
        permits SubscriptionPortfolioEventMetadata, SubscriptionConversionPortfolioEventMetadata {
}
