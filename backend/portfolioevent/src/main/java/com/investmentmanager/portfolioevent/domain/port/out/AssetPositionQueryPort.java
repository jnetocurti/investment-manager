package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;

import java.util.Optional;

public interface AssetPositionQueryPort {

    Optional<AssetPositionData> findByAssetNameAndAssetTypeAndBrokerKey(
            String assetName,
            AssetType assetType,
            String brokerKey);

    record AssetPositionData(
            String assetName,
            AssetType assetType,
            String brokerKey,
            int quantity,
            MonetaryValue averagePrice,
            MonetaryValue totalCost
    ) {
    }
}
