package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.commons.domain.model.AssetType;

import java.util.Optional;

public interface AssetPositionRepositoryPort {

    AssetPosition save(AssetPosition assetPosition);

    Optional<AssetPosition> findByAssetNameAndAssetTypeAndBrokerId(
            String assetName,
            AssetType assetType,
            String brokerId);
}
