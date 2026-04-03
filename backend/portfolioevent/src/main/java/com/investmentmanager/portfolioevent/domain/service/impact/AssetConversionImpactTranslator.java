package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.ConversionRatio;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssetConversionImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.ASSET_CONVERSION.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        String oldTicker = event.getMetadata() != null ? event.getMetadata().getOldTicker() : null;
        String newTicker = event.getMetadata() != null ? event.getMetadata().getNewTicker() : null;
        String ratioValue = event.getMetadata() != null ? event.getMetadata().getSplitRatio() : null;

        if (oldTicker == null || oldTicker.isBlank()) {
            throw new IllegalArgumentException("Ticker de origem é obrigatório para tradução de conversão");
        }
        if (newTicker == null || newTicker.isBlank()) {
            throw new IllegalArgumentException("Ticker de destino é obrigatório para tradução de conversão");
        }

        ConversionRatio ratio = ConversionRatio.parse(ratioValue);
        BigDecimal factor = ratio.factor();
        int sourceQuantity = event.getQuantity();
        BigDecimal rawTargetQuantity = BigDecimal.valueOf(sourceQuantity).multiply(factor);
        int targetQuantity = rawTargetQuantity.setScale(0, RoundingMode.DOWN).intValueExact();

        List<PositionImpactEvent> impacts = new ArrayList<>();

        impacts.add(PositionImpactEvent.builder()
                .originalEventId(event.getId())
                .ticker(oldTicker)
                .assetType(event.getAssetType())
                .impactType(PositionImpactType.DECREASE)
                .sequence(1)
                .quantity(sourceQuantity)
                .unitPrice(MonetaryValue.zero())
                .fee(MonetaryValue.zero())
                .eventDate(event.getEventDate())
                .originType(event.getEventType())
                .sourceType(ImpactSourceType.CORPORATE_ACTION)
                .brokerKey(event.getBrokerKey())
                .sourceReferenceId(event.getSourceReferenceId())
                .schemaVersion(1)
                .createdAt(LocalDateTime.now())
                .build());

        if (targetQuantity > 0) {
            MonetaryValue sourceAveragePrice = event.getUnitPrice();
            MonetaryValue sourceTotalCost = event.getTotalValue();
            MonetaryValue theoreticalPostConversionPrice = sourceAveragePrice.divide(factor);
            BigDecimal convertedFractionQuantity = rawTargetQuantity.subtract(BigDecimal.valueOf(targetQuantity));
            MonetaryValue fractionResidualBookValue = theoreticalPostConversionPrice.multiply(convertedFractionQuantity);
            MonetaryValue convertedTotalCost = sourceTotalCost.subtract(fractionResidualBookValue);
            MonetaryValue targetAveragePrice = convertedTotalCost.divide(BigDecimal.valueOf(targetQuantity));

            impacts.add(PositionImpactEvent.builder()
                    .originalEventId(event.getId())
                    .ticker(newTicker)
                    .assetType(event.getAssetType())
                    .impactType(PositionImpactType.INCREASE)
                    .sequence(2)
                    .quantity(targetQuantity)
                    .unitPrice(targetAveragePrice)
                    .fee(MonetaryValue.zero())
                    .eventDate(event.getEventDate())
                    .originType(event.getEventType())
                    .sourceType(ImpactSourceType.CORPORATE_ACTION)
                    .brokerKey(event.getBrokerKey())
                    .sourceReferenceId(event.getSourceReferenceId())
                    .schemaVersion(1)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return impacts;
    }
}
