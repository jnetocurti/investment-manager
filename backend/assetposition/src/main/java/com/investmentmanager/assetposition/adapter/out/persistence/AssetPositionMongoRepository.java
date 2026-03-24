package com.investmentmanager.assetposition.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface AssetPositionMongoRepository extends MongoRepository<AssetPositionDocument, String> {

    Optional<AssetPositionDocument> findByAssetNameAndBrokerDocument(String assetName, String brokerDocument);
}
