package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.PortfolioEventData;

import java.util.List;

public interface PortfolioEventQueryPort {

    List<PortfolioEventData> findByAssetNameAndBrokerIdOrderByEventDate(
            String assetName, String brokerId);
}
