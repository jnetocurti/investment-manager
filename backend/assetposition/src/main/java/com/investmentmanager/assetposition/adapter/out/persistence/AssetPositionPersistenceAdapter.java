package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class AssetPositionPersistenceAdapter implements AssetPositionRepositoryPort {

    private final AssetPositionMongoRepository repository;

    @Override
    public AssetPosition save(AssetPosition assetPosition) {
        var saved = repository.save(AssetPositionDocumentMapper.toDocument(assetPosition));
        return AssetPositionDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<AssetPosition> findByAssetNameAndAssetTypeAndBrokerId(
            String assetName,
            AssetType assetType,
            String brokerId) {
        return repository.findByAssetNameAndAssetTypeAndBrokerId(
                        assetName,
                        assetType != null ? assetType.name() : null,
                        brokerId)
                .map(AssetPositionDocumentMapper::toDomain);
    }
}
