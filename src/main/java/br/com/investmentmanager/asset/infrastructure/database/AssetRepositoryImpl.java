package br.com.investmentmanager.asset.infrastructure.database;

import br.com.investmentmanager.asset.domain.Asset;
import br.com.investmentmanager.asset.domain.AssetRepository;
import br.com.investmentmanager.asset.domain.valueobjects.PortfolioEvent;
import br.com.investmentmanager.asset.domain.valueobjects.PositionHistory;
import br.com.investmentmanager.asset.infrastructure.database.model.PersistenceAsset;
import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

import static java.util.stream.Collectors.toCollection;

@Repository
@RequiredArgsConstructor
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetDAO assetDAO;

    @Override
    public Optional<Asset> findOne(String assetCode, AssetType assetType) {
        var example = new PersistenceAsset();
        example.setAssetCode(assetCode);
        example.setAssetType(assetType);

        return assetDAO.findOne(Example.of(example)).map(a -> Asset.builder()
                .id(a.getId())
                .assetCode(a.getAssetCode())
                .assetType(a.getAssetType())
                .quantity(a.getQuantity())
                .averagePurchaseCost(a.getAveragePurchaseCost())
                .portfolioEvents(a.getPortfolioEvents().stream().map(e -> PortfolioEvent.of(
                        e.getInvoiceNumber(),
                        e.getOperation(),
                        e.getQuantity(),
                        e.getTradingDate(),
                        e.getAveragePurchaseCost())).collect(toCollection(ArrayList::new))
                )
                .positionHistory(a.getPositionHistory().stream().map(p -> PositionHistory.of(
                        p.getDate(),
                        p.getQuantity(),
                        p.getAveragePurchaseCost())).collect(toCollection(ArrayList::new))
                ).build());
    }

    @Override
    public Asset save(Asset asset) {
        PersistenceAsset map = new ModelMapper().map(asset, PersistenceAsset.class);
        return new ModelMapper().map(assetDAO.save(map), Asset.AssetBuilder.class).build();
    }
}
