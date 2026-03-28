package com.investmentmanager.portfolioevent.adapter.out.persistence.impact;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PositionImpactEventMongoRepository extends MongoRepository<PositionImpactEventDocument, String> {

    boolean existsByOriginalEventIdAndImpactTypeAndSequence(String originalEventId, String impactType, int sequence);

    @Query(value = "{'ticker': ?0, 'assetType': ?1, '$or': [ {'brokerDocument': {'$in': ?2}}, {'brokerName': {'$in': ?3}} ]}",
            sort = "{'eventDate': 1, 'sequence': 1}")
    List<PositionImpactEventDocument> findByTickerAndAssetTypeAndBrokerAliases(
            String ticker,
            String assetType,
            List<String> brokerDocuments,
            List<String> brokerNames);
}
