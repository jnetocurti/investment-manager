package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.commons.domain.model.AssetType;

import java.util.List;

public interface AssetPositionHistoryRepositoryPort {

    void saveAll(List<AssetPositionSnapshot> snapshots, String assetName, AssetType assetType, String brokerDocument);

    void deleteByAssetNameAndAssetTypeAndBrokerDocument(String assetName, AssetType assetType, String brokerDocument);
}
