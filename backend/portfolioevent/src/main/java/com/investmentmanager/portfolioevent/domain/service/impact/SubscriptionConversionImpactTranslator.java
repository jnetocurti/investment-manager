package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.commons.domain.model.PositionImpactType;

import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionConversionImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.SUBSCRIPTION_CONVERSION.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        return List.of(PositionImpactEvent.builder()
                .originalEventId(event.getId())
                .ticker(event.getAssetName())
                .assetType(event.getAssetType())
                .impactType(PositionImpactType.INCREASE)
                .sequence(1)
                .quantity(event.getQuantity())
                .unitPrice(event.getUnitPrice())
                .fee(event.getFee())
                .eventDate(event.getEventDate())
                .originType(event.getEventType())
                .sourceType(ImpactSourceType.SUBSCRIPTION)
                .brokerKey(event.getBrokerKey())
                
                .sourceReferenceId(event.getSourceReferenceId())
                .schemaVersion(1)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
