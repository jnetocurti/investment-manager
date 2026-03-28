package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class AssetPositionHistoryPersistenceAdapter implements AssetPositionHistoryRepositoryPort {

    private final AssetPositionHistoryMongoRepository mongoRepository;

    @Override
    public void saveAll(List<AssetPositionSnapshot> snapshots, String assetName, String brokerKey) {
        var docs = snapshots.stream().map(snapshot -> {
            var doc = new AssetPositionHistoryDocument();
            doc.setAssetName(assetName);
            doc.setBrokerKey(brokerKey);
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
    public void deleteByAssetNameAndBrokerKey(String assetName, String brokerKey) {
        mongoRepository.deleteByAssetNameAndBrokerKey(assetName, brokerKey);
    }
}
