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

    private final AssetPositionMongoRepository mongoRepository;

    @Override
    public AssetPosition save(AssetPosition position) {
        var doc = AssetPositionDocumentMapper.toDocument(position);
        var saved = mongoRepository.save(doc);
        return AssetPositionDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<AssetPosition> findByAssetNameAndAssetTypeAndBrokerDocument(
            String assetName,
            AssetType assetType,
            String brokerDocument) {
        return mongoRepository.findByAssetNameAndAssetTypeAndBrokerDocument(
                        assetName,
                        assetType != null ? assetType.name() : null,
                        brokerDocument)
                .map(AssetPositionDocumentMapper::toDomain);
    }
}
