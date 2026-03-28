package com.investmentmanager.portfolioevent.adapter.out.persistence.impact;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.commons.domain.model.adjustment.*;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
        doc.setUnitPrice(event.getUnitPrice() != null ? event.getUnitPrice().toDisplayValue() : BigDecimal.ZERO);
        doc.setFee(event.getFee() != null ? event.getFee().toDisplayValue() : BigDecimal.ZERO);
        if (event.getAdjustmentType() != null) {
            doc.setAdjustmentType(event.getAdjustmentType().name());
            doc.setAdjustmentPayload(toMap(event.getAdjustmentPayload()));
        }
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

        AdjustmentType adjustmentType = doc.getAdjustmentType() != null
                ? AdjustmentType.valueOf(doc.getAdjustmentType()) : null;

        return PositionImpactEvent.builder()
                .id(doc.getId())
                .originalEventId(doc.getOriginalEventId())
                .ticker(doc.getTicker())
                .assetType(doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null)
                .impactType(PositionImpactType.valueOf(doc.getImpactType()))
                .sequence(doc.getSequence())
                .quantity(doc.getQuantity())
                .unitPrice(MonetaryValue.of(doc.getUnitPrice() != null ? doc.getUnitPrice() : BigDecimal.ZERO))
                .fee(MonetaryValue.of(doc.getFee() != null ? doc.getFee() : BigDecimal.ZERO))
                .adjustmentType(adjustmentType)
                .adjustmentPayload(toPayload(adjustmentType, doc.getAdjustmentPayload()))
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

    private static Map<String, Object> toMap(AdjustmentPayload payload) {
        if (payload == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        switch (payload.getType()) {
            case FACTOR -> {
                FactorAdjustmentPayload factorPayload = (FactorAdjustmentPayload) payload;
                map.put("numerator", factorPayload.getRatio().getNumerator());
                map.put("denominator", factorPayload.getRatio().getDenominator());
            }
            case ABSOLUTE -> {
                AbsoluteAdjustmentPayload absolute = (AbsoluteAdjustmentPayload) payload;
                map.put("targetQuantity", absolute.getTargetQuantity());
                map.put("targetAveragePrice", absolute.getTargetAveragePrice().toDisplayValue());
            }
        }
        return map;
    }

    private static AdjustmentPayload toPayload(AdjustmentType type, Map<String, Object> payloadMap) {
        if (type == null || payloadMap == null) {
            return null;
        }

        return switch (type) {
            case FACTOR -> FactorAdjustmentPayload.builder()
                    .ratio(Ratio.builder()
                            .numerator(new BigDecimal(String.valueOf(payloadMap.get("numerator"))))
                            .denominator(new BigDecimal(String.valueOf(payloadMap.get("denominator"))))
                            .build())
                    .build();
            case ABSOLUTE -> AbsoluteAdjustmentPayload.builder()
                    .targetQuantity(Integer.parseInt(String.valueOf(payloadMap.get("targetQuantity"))))
                    .targetAveragePrice(MonetaryValue.of(new BigDecimal(String.valueOf(payloadMap.get("targetAveragePrice")))))
                    .build();
        };
    }
}
