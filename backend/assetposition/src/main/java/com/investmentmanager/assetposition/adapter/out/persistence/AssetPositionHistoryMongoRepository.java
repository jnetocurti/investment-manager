package com.investmentmanager.assetposition.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

interface AssetPositionHistoryMongoRepository extends MongoRepository<AssetPositionHistoryDocument, String> {

    void deleteByAssetNameAndBrokerKey(String assetName, String brokerKey);
}
