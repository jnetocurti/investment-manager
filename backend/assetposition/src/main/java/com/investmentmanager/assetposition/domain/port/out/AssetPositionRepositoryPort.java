package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.AssetPosition;

import java.util.Optional;

public interface AssetPositionRepositoryPort {

    AssetPosition save(AssetPosition position);

    Optional<AssetPosition> findByAssetNameAndBrokerDocument(String assetName, String brokerDocument);
}
