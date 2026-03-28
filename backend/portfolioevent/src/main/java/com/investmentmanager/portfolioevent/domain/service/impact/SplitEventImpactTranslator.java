package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.commons.domain.model.adjustment.AdjustmentType;
import com.investmentmanager.commons.domain.model.adjustment.FactorAdjustmentPayload;
import com.investmentmanager.portfolioevent.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;

public class SplitEventImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.SPLIT.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        return List.of(PositionImpactEvent.builder()
                .originalEventId(event.getId())
                .ticker(event.getAssetName())
                .assetType(event.getAssetType())
                .impactType(PositionImpactType.ADJUST)
                .sequence(1)
                .quantity(0)
                .unitPrice(com.investmentmanager.commons.domain.model.MonetaryValue.zero())
                .fee(com.investmentmanager.commons.domain.model.MonetaryValue.zero())
                .adjustmentType(AdjustmentType.FACTOR)
                .adjustmentPayload(FactorAdjustmentPayload.builder().ratio(event.getRatio()).build())
                .eventDate(event.getEventDate())
                .originType(event.getEventType())
                .sourceType(ImpactSourceType.CORPORATE_ACTION)
                .brokerName(event.getBrokerName())
                .brokerDocument(event.getBrokerDocument())
                .sourceReferenceId(event.getSourceReferenceId())
                .schemaVersion(2)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
