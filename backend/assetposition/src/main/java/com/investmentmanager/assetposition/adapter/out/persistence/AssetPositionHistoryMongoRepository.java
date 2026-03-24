package com.investmentmanager.assetposition.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

interface AssetPositionHistoryMongoRepository extends MongoRepository<AssetPositionHistoryDocument, String> {

    void deleteByAssetNameAndBrokerDocument(String assetName, String brokerDocument);
}
