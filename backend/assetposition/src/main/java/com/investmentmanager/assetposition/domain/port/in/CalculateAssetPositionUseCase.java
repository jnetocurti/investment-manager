package com.investmentmanager.assetposition.domain.port.in;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.commons.domain.model.AssetType;

public interface CalculateAssetPositionUseCase {

    AssetPosition calculatePosition(String assetName, AssetType assetType, String brokerId);
}
