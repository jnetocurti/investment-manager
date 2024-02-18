package br.com.investmentmanager.asset.infrastructure.database;

import br.com.investmentmanager.asset.domain.Asset;
import br.com.investmentmanager.asset.domain.AssetRepository;
import br.com.investmentmanager.asset.infrastructure.database.model.PersistenceAsset;
import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetDAO assetDAO;

    @Override
    public Optional<Asset> findOne(String assetCode, AssetType assetType) {
        var example = new PersistenceAsset();
        example.setAssetCode(assetCode);
        example.setAssetType(assetType);

        return assetDAO.findOne(Example.of(example))
                .map(a -> new ModelMapper().map(a, Asset.AssetBuilder.class).build());
    }

    @Override
    public Asset save(Asset asset) {
        PersistenceAsset map = new ModelMapper().map(asset, PersistenceAsset.class);
        return new ModelMapper().map(assetDAO.save(map), Asset.AssetBuilder.class).build();
    }
}
