package com.investmentmanager.portfolioevent.domain.model;

import java.math.BigDecimal;
import java.util.List;

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
    private final String genericObservation;
    private final List<GenericAssetSnapshot> genericAssets;
    private final BigDecimal genericResidualAmount;
    private final String genericResidualDescription;

    @Getter
    @Builder(toBuilder = true)
    public static class GenericAssetSnapshot {
        private final String ticker;
        private final String assetType;
        private final Integer quantity;
        private final BigDecimal averagePrice;
    }

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
