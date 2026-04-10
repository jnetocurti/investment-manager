package com.investmentmanager.portfolioevent.domain.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PortfolioEventMetadata {

    private final String subscriptionTicker;
    private final String splitRatio;
    private final String oldTicker;
    private final String newTicker;
    private final String bonusRatio;
    private final Integer bonusBaseQuantity;
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

    public static PortfolioEventMetadata tickerRename(String oldTicker, String newTicker) {
        return PortfolioEventMetadata.builder()
                .oldTicker(oldTicker)
                .newTicker(newTicker)
                .build();
    }

    public static PortfolioEventMetadata bonus(String bonusRatio, Integer bonusBaseQuantity) {
        return PortfolioEventMetadata.builder()
                .bonusRatio(bonusRatio)
                .bonusBaseQuantity(bonusBaseQuantity)
                .build();
    }
}
