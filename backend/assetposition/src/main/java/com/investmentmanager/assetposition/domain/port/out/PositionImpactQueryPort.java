package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;

import java.util.List;

public interface PositionImpactQueryPort {

    List<PositionImpactData> findByTickerAndBrokerDocument(String ticker, String brokerDocument);
}
