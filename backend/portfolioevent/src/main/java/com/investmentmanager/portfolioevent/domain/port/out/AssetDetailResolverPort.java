package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.commons.domain.model.AssetType;

/**
 * Resolve a descrição do ativo oriunda da nota de negociação
 * para ticker normalizado e tipo de ativo.
 */
public interface AssetDetailResolverPort {

    AssetDetail resolve(String assetDescription);

    record AssetDetail(String ticker, AssetType assetType) {}
}
