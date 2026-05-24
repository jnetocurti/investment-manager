package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.RealizedSaleResult;

import java.util.List;

public interface RealizedSaleResultRepositoryPort {

    void upsertAllByAssetAndBroker(String assetName, String brokerKey, List<RealizedSaleResult> results);
}
