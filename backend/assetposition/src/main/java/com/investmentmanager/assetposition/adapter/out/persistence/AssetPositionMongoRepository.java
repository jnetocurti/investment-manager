package com.investmentmanager.assetposition.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface AssetPositionMongoRepository extends MongoRepository<AssetPositionDocument, String> {

    Optional<AssetPositionDocument> findByAssetNameAndAssetTypeAndBrokerDocument(
            String assetName, String assetType, String brokerDocument);
}
