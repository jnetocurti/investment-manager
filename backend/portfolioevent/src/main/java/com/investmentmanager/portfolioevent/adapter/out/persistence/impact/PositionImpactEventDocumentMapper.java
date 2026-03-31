package com.investmentmanager.portfolioevent.adapter.out.persistence.impact;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PositionImpactEventDocumentMapper {

    public static PositionImpactEventDocument toDocument(PositionImpactEvent event) {
        var doc = new PositionImpactEventDocument();
        doc.setId(event.getId());
        doc.setOriginalEventId(event.getOriginalEventId());
        doc.setTicker(event.getTicker());
        doc.setAssetType(event.getAssetType() != null ? event.getAssetType().name() : null);
        doc.setImpactType(event.getImpactType().name());
        doc.setSequence(event.getSequence());
        doc.setQuantity(event.getQuantity());
        doc.setUnitPrice(event.getUnitPrice() != null ? event.getUnitPrice().toDisplayValue() : null);
        doc.setFee(event.getFee() != null ? event.getFee().toDisplayValue() : null);
        doc.setFactor(event.getFactor());
        doc.setAdjustmentType(event.getAdjustmentType() != null ? event.getAdjustmentType().name() : null);
        doc.setEventDate(event.getEventDate());
        doc.setOriginType(event.getOriginType().name());
        doc.setSourceType(event.getSourceType().name());
        doc.setBrokerKey(event.getBrokerKey());
        doc.setSourceReferenceId(event.getSourceReferenceId());
        doc.setSchemaVersion(event.getSchemaVersion());
        doc.setCreatedAt(event.getCreatedAt());
        return doc;
    }

    public static PositionImpactEvent toDomain(PositionImpactEventDocument doc) {
        return PositionImpactEvent.builder()
                .id(doc.getId())
                .originalEventId(doc.getOriginalEventId())
                .ticker(doc.getTicker())
                .assetType(doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null)
                .impactType(PositionImpactType.valueOf(doc.getImpactType()))
                .sequence(doc.getSequence())
                .quantity(doc.getQuantity())
                .unitPrice(MonetaryValue.of(doc.getUnitPrice()))
                .fee(MonetaryValue.of(doc.getFee()))
                .factor(doc.getFactor())
                .adjustmentType(doc.getAdjustmentType() != null
                        ? PositionAdjustmentType.valueOf(doc.getAdjustmentType())
                        : null)
                .eventDate(doc.getEventDate())
                .originType(EventType.valueOf(doc.getOriginType()))
                .sourceType(ImpactSourceType.valueOf(doc.getSourceType()))
                .brokerKey(doc.getBrokerKey())
                .sourceReferenceId(doc.getSourceReferenceId())
                .schemaVersion(doc.getSchemaVersion() != null ? doc.getSchemaVersion() : 1)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
