package com.investmentmanager.assetposition.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface AssetPositionMongoRepository extends MongoRepository<AssetPositionDocument, String> {

    Optional<AssetPositionDocument> findByAssetNameAndAssetTypeAndBrokerKey(
            String assetName,
            String assetType,
            String brokerKey);
}
