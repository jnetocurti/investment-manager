package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.SplitRatio;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.time.LocalDateTime;
import java.util.List;

public class SplitImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.SPLIT.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        String ratioValue = event.getMetadata() != null ? event.getMetadata().getSplitRatio() : null;
        SplitRatio ratio = SplitRatio.parse(ratioValue);

        return List.of(PositionImpactEvent.builder()
                .originalEventId(event.getId())
                .ticker(event.getAssetName())
                .assetType(event.getAssetType())
                .impactType(PositionImpactType.ADJUST)
                .sequence(1)
                .quantity(1)
                .unitPrice(MonetaryValue.zero())
                .fee(MonetaryValue.zero())
                .factor(ratio.factor())
                .adjustmentType(ratio.factor().compareTo(java.math.BigDecimal.ONE) >= 0
                        ? PositionAdjustmentType.SPLIT
                        : PositionAdjustmentType.REVERSE_SPLIT)
                .eventDate(event.getEventDate())
                .originType(event.getEventType())
                .sourceType(ImpactSourceType.CORPORATE_ACTION)
                .brokerKey(event.getBrokerKey())
                .sourceReferenceId(event.getSourceReferenceId())
                .schemaVersion(1)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
