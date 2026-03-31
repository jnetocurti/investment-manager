package com.investmentmanager.assetposition.adapter.out.query;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.adapter.out.persistence.impact.PositionImpactEventDocument;
import com.investmentmanager.portfolioevent.adapter.out.persistence.impact.PositionImpactEventMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class PositionImpactQueryAdapter implements PositionImpactQueryPort {

    private final PositionImpactEventMongoRepository repository;

    @Override
    public List<PositionImpactData> findByTickerAndAssetTypeAndBrokerKey(
            String ticker,
            AssetType assetType,
            String brokerKey) {
        List<PositionImpactEventDocument> docs =
                repository.findByTickerAndAssetTypeAndBrokerKeyOrderByEventDateAscSequenceAsc(
                        ticker,
                        assetType != null ? assetType.name() : null,
                        brokerKey);

        return docs.stream()
                .map(doc -> PositionImpactData.builder()
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
                        .originType(doc.getOriginType())
                        .sourceType(doc.getSourceType())
                        .brokerKey(doc.getBrokerKey())
                        .sourceReferenceId(doc.getSourceReferenceId())
                        .schemaVersion(doc.getSchemaVersion() != null ? doc.getSchemaVersion() : 1)
                        .createdAt(doc.getCreatedAt())
                        .build())
                .toList();
    }
}
