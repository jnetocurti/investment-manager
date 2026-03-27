package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.AssetPosition;

import com.investmentmanager.commons.domain.model.AssetType;

import java.util.Optional;

public interface AssetPositionRepositoryPort {

    AssetPosition save(AssetPosition position);

    Optional<AssetPosition> findByAssetNameAndAssetTypeAndBrokerDocument(
            String assetName,
            AssetType assetType,
            String brokerDocument);
}
