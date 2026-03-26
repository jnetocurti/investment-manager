package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactType;

import java.time.LocalDateTime;
import java.util.List;

public class SellEventImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.SELL.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        return List.of(PositionImpactEvent.builder()
                .originalEventId(event.getId())
                .ticker(event.getAssetName())
                .impactType(PositionImpactType.DECREASE)
                .sequence(1)
                .quantity(event.getQuantity())
                .unitPrice(event.getUnitPrice())
                .fee(event.getFee())
                .eventDate(event.getEventDate())
                .originType(event.getEventType())
                .sourceType(ImpactSourceType.TRADING_NOTE)
                .brokerName(event.getBrokerName())
                .brokerDocument(event.getBrokerDocument())
                .sourceReferenceId(event.getSourceReferenceId())
                .schemaVersion(1)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
