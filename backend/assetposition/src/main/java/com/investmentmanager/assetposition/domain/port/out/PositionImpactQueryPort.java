package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.AssetType;

import java.util.List;

public interface PositionImpactQueryPort {

    List<PositionImpactData> findByTickerAndAssetTypeAndBrokerKey(
            String ticker,
            AssetType assetType,
            String brokerKey);
}
