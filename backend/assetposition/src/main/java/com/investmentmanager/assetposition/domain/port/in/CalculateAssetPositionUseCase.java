package com.investmentmanager.assetposition.domain.port.in;

import com.investmentmanager.assetposition.domain.model.AssetPosition;

public interface CalculateAssetPositionUseCase {

    AssetPosition calculatePosition(String assetName, String brokerDocument);
}
