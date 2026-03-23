package com.investmentmanager.asset.domain.port.out;

import com.investmentmanager.asset.domain.model.Asset;

import java.util.Optional;

public interface AssetRepositoryPort {

    Asset save(Asset asset);

    Optional<Asset> findByTicker(String ticker);
}
