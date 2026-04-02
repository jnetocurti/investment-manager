package com.investmentmanager.portfolioevent.adapter.out.query;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface AssetPositionQueryMongoRepository extends MongoRepository<AssetPositionQueryDocument, String> {

    Optional<AssetPositionQueryDocument> findByAssetNameAndAssetTypeAndBrokerKey(
            String assetName,
            String assetType,
            String brokerKey);
}
