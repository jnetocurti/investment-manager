package com.investmentmanager.portfolioevent.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PortfolioEventMetadata {

    private final String subscriptionTicker;
    private final String splitRatio;

    public static PortfolioEventMetadata subscription(String subscriptionTicker) {
        return PortfolioEventMetadata.builder()
                .subscriptionTicker(subscriptionTicker)
                .build();
    }

    public static PortfolioEventMetadata split(String splitRatio) {
        return PortfolioEventMetadata.builder()
                .splitRatio(splitRatio)
                .build();
    }
}
