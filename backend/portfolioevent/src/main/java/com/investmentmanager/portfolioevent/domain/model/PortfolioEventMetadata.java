package com.investmentmanager.portfolioevent.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PortfolioEventMetadata {

    private final String subscriptionTicker;

    public static PortfolioEventMetadata subscription(String subscriptionTicker) {
        return PortfolioEventMetadata.builder()
                .subscriptionTicker(subscriptionTicker)
                .build();
    }
}
