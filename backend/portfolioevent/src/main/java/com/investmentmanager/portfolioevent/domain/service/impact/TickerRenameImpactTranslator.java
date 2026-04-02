package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.time.LocalDateTime;
import java.util.List;

public class TickerRenameImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.TICKER_RENAME.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        String oldTicker = event.getMetadata() != null ? event.getMetadata().getOldTicker() : null;
        String newTicker = event.getMetadata() != null ? event.getMetadata().getNewTicker() : null;

        if (oldTicker == null || oldTicker.isBlank()) {
            throw new IllegalArgumentException("Ticker de origem é obrigatório para tradução de renomeação");
        }
        if (newTicker == null || newTicker.isBlank()) {
            throw new IllegalArgumentException("Ticker de destino é obrigatório para tradução de renomeação");
        }

        PositionImpactEvent decreaseOldTicker = PositionImpactEvent.builder()
                .originalEventId(event.getId())
                .ticker(oldTicker)
                .assetType(event.getAssetType())
                .impactType(PositionImpactType.DECREASE)
                .sequence(1)
                .quantity(event.getQuantity())
                .unitPrice(MonetaryValue.zero())
                .fee(MonetaryValue.zero())
                .eventDate(event.getEventDate())
                .originType(event.getEventType())
                .sourceType(ImpactSourceType.CORPORATE_ACTION)
                .brokerKey(event.getBrokerKey())
                .sourceReferenceId(event.getSourceReferenceId())
                .schemaVersion(1)
                .createdAt(LocalDateTime.now())
                .build();

        PositionImpactEvent increaseNewTicker = PositionImpactEvent.builder()
                .originalEventId(event.getId())
                .ticker(newTicker)
                .assetType(event.getAssetType())
                .impactType(PositionImpactType.INCREASE)
                .sequence(2)
                .quantity(event.getQuantity())
                .unitPrice(event.getUnitPrice())
                .fee(MonetaryValue.zero())
                .eventDate(event.getEventDate())
                .originType(event.getEventType())
                .sourceType(ImpactSourceType.CORPORATE_ACTION)
                .brokerKey(event.getBrokerKey())
                .sourceReferenceId(event.getSourceReferenceId())
                .schemaVersion(1)
                .createdAt(LocalDateTime.now())
                .build();

        return List.of(decreaseOldTicker, increaseNewTicker);
    }
}
