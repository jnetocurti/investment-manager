package com.investmentmanager.portfolioevent.domain.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PortfolioEventMetadata {

    private final String subscriptionTicker;
    private final String splitRatio;
    private final BigDecimal splitFractionResidualBookValue;
    private final String splitFractionFlowStatus;
    private final String splitFractionSourceReferenceId;

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
