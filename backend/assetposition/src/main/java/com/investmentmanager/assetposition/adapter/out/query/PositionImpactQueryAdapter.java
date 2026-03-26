package com.investmentmanager.assetposition.adapter.out.query;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.adapter.out.persistence.impact.PositionImpactEventMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class PositionImpactQueryAdapter implements PositionImpactQueryPort {

    private final PositionImpactEventMongoRepository repository;

    @Override
    public List<PositionImpactData> findByTickerAndAssetTypeAndBrokerDocument(
            String ticker,
            AssetType assetType,
            String brokerDocument) {
        return repository.findByTickerAndBrokerDocumentOrderByEventDateAscSequenceAsc(
                        ticker,
                        assetType != null ? assetType.name() : null,
                        brokerDocument)
                .stream()
                .map(doc -> PositionImpactData.builder()
                        .id(doc.getId())
                        .originalEventId(doc.getOriginalEventId())
                        .ticker(doc.getTicker())
                        .assetType(doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null)
                        .impactType(doc.getImpactType())
                        .sequence(doc.getSequence())
                        .quantity(doc.getQuantity())
                        .unitPrice(MonetaryValue.of(doc.getUnitPrice()))
                        .fee(MonetaryValue.of(doc.getFee()))
                        .factor(doc.getFactor())
                        .eventDate(doc.getEventDate())
                        .originType(doc.getOriginType())
                        .sourceType(doc.getSourceType())
                        .brokerName(doc.getBrokerName())
                        .brokerDocument(doc.getBrokerDocument())
                        .sourceReferenceId(doc.getSourceReferenceId())
                        .schemaVersion(doc.getSchemaVersion() != null ? doc.getSchemaVersion() :
                                (doc.getVersion() != null ? doc.getVersion() : 1))
                        .createdAt(doc.getCreatedAt())
                        .build())
                .toList();
    }
}
