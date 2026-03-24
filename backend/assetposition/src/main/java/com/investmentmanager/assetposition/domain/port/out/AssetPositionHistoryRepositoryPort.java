package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;

import java.util.List;

public interface AssetPositionHistoryRepositoryPort {

    void saveAll(List<AssetPositionSnapshot> snapshots, String assetName, String brokerDocument);

    void deleteByAssetNameAndBrokerDocument(String assetName, String brokerDocument);
}
