package com.investmentmanager.portfolioevent.adapter.out.persistence.impact;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PositionImpactEventDocumentMapper {

    public static PositionImpactEventDocument toDocument(PositionImpactEvent event) {
        PositionImpactEventDocument doc = new PositionImpactEventDocument();
        doc.setId(event.getId());
        doc.setOriginalEventId(event.getOriginalEventId());
        doc.setTicker(event.getTicker());
        doc.setAssetType(event.getAssetType() != null ? event.getAssetType().name() : null);
        doc.setImpactType(event.getImpactType().name());
        doc.setSequence(event.getSequence());
        doc.setQuantity(event.getQuantity());
        doc.setUnitPrice(event.getUnitPrice().toDisplayValue());
        doc.setFee(event.getFee().toDisplayValue());
        doc.setFactor(event.getFactor());
        doc.setEventDate(event.getEventDate());
        doc.setOriginType(event.getOriginType().name());
        doc.setSourceType(event.getSourceType().name());
        doc.setBrokerName(event.getBrokerName());
        doc.setBrokerDocument(event.getBrokerDocument());
        doc.setSourceReferenceId(event.getSourceReferenceId());
        doc.setSchemaVersion(event.getSchemaVersion());
        doc.setVersion(event.getSchemaVersion());
        doc.setCreatedAt(event.getCreatedAt());
        return doc;
    }

    public static PositionImpactEvent toDomain(PositionImpactEventDocument doc) {
        int schemaVersion = doc.getSchemaVersion() != null
                ? doc.getSchemaVersion()
                : (doc.getVersion() != null ? doc.getVersion() : 1);

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
                .eventDate(doc.getEventDate())
                .originType(EventType.valueOf(doc.getOriginType()))
                .sourceType(ImpactSourceType.valueOf(doc.getSourceType()))
                .brokerName(doc.getBrokerName())
                .brokerDocument(doc.getBrokerDocument())
                .sourceReferenceId(doc.getSourceReferenceId())
                .schemaVersion(schemaVersion)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
