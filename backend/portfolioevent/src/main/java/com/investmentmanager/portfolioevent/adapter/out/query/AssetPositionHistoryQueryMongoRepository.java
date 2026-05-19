package com.investmentmanager.portfolioevent.adapter.out.query;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

interface AssetPositionHistoryQueryMongoRepository extends MongoRepository<AssetPositionHistoryQueryDocument, String> {

    Optional<AssetPositionHistoryQueryDocument> findFirstByAssetNameAndBrokerKeyAndEventDateLessThanEqualOrderByEventDateDescEventOrderDescRecordedAtDesc(
            String assetName,
            String brokerKey,
            LocalDate eventDate);
}
