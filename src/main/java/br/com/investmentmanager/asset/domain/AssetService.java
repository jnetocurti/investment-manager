package br.com.investmentmanager.asset.domain;

import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository repository;

    public Asset getOrCreate(String assetCode, AssetType assetType) {
        return repository.findOne(assetCode, assetType)
                .orElse(Asset.builder().assetCode(assetCode).assetType(assetType).build());
    }

    public Asset save(Asset asset) {
        return repository.save(asset);
    }
}
