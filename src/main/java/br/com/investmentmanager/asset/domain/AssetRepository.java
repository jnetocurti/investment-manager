package br.com.investmentmanager.asset.domain;

import br.com.investmentmanager.shared.util.constants.AssetType;

import java.util.Optional;

public interface AssetRepository {

    Optional<Asset> findOne(String assetCode, AssetType assetType);

    Asset save(Asset asset);
}
