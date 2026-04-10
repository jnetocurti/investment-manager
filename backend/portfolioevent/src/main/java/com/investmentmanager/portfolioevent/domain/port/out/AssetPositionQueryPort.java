package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;

import java.time.LocalDate;
import java.util.Optional;

public interface AssetPositionQueryPort {

    Optional<AssetPositionData> findByAssetNameAndAssetTypeAndBrokerKey(
            String assetName,
            AssetType assetType,
            String brokerKey);

    Optional<Integer> findQuantityAsOfDate(
            String assetName,
            String brokerKey,
            LocalDate asOfDate);

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
