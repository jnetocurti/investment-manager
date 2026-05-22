package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GenericCorporateActionImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.GENERIC_CORPORATE_ACTION.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        if (event.getMetadata() == null || event.getMetadata().getGenericAssets() == null || event.getMetadata().getGenericAssets().isEmpty()) {
            throw new IllegalArgumentException("Evento genérico sem ativos para tradução");
        }
        List<PositionImpactEvent> impacts = new ArrayList<>();
        int sequence = 1;
        for (var asset : event.getMetadata().getGenericAssets()) {
            impacts.add(PositionImpactEvent.builder()
                    .originalEventId(event.getId())
                    .ticker(asset.getTicker())
                    .assetType(AssetType.valueOf(asset.getAssetType()))
                    .impactType(PositionImpactType.ADJUST)
                    .sequence(sequence++)
                    .quantity(asset.getQuantity())
                    .unitPrice(MonetaryValue.of(asset.getAveragePrice()))
                    .fee(MonetaryValue.zero())
                    .eventDate(event.getEventDate())
                    .originType(event.getEventType())
                    .sourceType(ImpactSourceType.CORPORATE_ACTION)
                    .brokerKey(event.getBrokerKey())
                    .sourceReferenceId(event.getSourceReferenceId())
                    .eventOrder(event.getEventOrder())
                    .schemaVersion(1)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        return impacts;
    }
}
