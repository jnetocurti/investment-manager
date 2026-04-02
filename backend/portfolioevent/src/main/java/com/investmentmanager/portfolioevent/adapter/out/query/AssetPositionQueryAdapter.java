package com.investmentmanager.portfolioevent.adapter.out.query;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.port.out.AssetPositionQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class AssetPositionQueryAdapter implements AssetPositionQueryPort {

    private final AssetPositionQueryMongoRepository repository;

    @Override
    public Optional<AssetPositionData> findByAssetNameAndAssetTypeAndBrokerKey(
            String assetName,
            AssetType assetType,
            String brokerKey) {
        return repository.findByAssetNameAndAssetTypeAndBrokerKey(
                        assetName,
                        assetType != null ? assetType.name() : null,
                        brokerKey)
                .map(doc -> new AssetPositionData(
                        doc.getAssetName(),
                        doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null,
                        doc.getBrokerKey(),
                        doc.getQuantity(),
                        MonetaryValue.of(doc.getAveragePrice()),
                        MonetaryValue.of(doc.getTotalCost())));
    }
}
