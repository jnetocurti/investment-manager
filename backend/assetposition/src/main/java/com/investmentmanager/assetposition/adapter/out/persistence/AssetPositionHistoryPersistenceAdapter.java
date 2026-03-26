package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class AssetPositionHistoryPersistenceAdapter implements AssetPositionHistoryRepositoryPort {

    private final AssetPositionHistoryMongoRepository mongoRepository;

    @Override
    public void saveAll(List<AssetPositionSnapshot> snapshots, String assetName, AssetType assetType, String brokerDocument) {
        var docs = snapshots.stream().map(snapshot -> {
            var doc = new AssetPositionHistoryDocument();
            doc.setAssetName(assetName);
            doc.setAssetType(assetType != null ? assetType.name() : null);
            doc.setBrokerDocument(brokerDocument);
            doc.setQuantity(snapshot.getQuantity());
            doc.setAveragePrice(snapshot.getAveragePrice().toDisplayValue());
            doc.setTotalCost(snapshot.getTotalCost().toDisplayValue());
            doc.setEventDate(snapshot.getEventDate());
            doc.setSourceType(snapshot.getSourceType());
            doc.setSourceReferenceId(snapshot.getSourceReferenceId());
            doc.setObservation(snapshot.getObservation());
            doc.setRecordedAt(snapshot.getRecordedAt());
            return doc;
        }).toList();
        mongoRepository.saveAll(docs);
    }

    @Override
    public void deleteByAssetNameAndAssetTypeAndBrokerDocument(String assetName, AssetType assetType, String brokerDocument) {
        mongoRepository.deleteByAssetNameAndAssetTypeAndBrokerDocument(
                assetName, assetType != null ? assetType.name() : null, brokerDocument);
    }
}
