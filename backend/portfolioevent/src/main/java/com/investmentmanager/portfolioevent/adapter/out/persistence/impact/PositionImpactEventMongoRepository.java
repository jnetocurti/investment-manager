package com.investmentmanager.portfolioevent.adapter.out.persistence.impact;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PositionImpactEventMongoRepository extends MongoRepository<PositionImpactEventDocument, String> {

    boolean existsByOriginalEventIdAndImpactTypeAndSequence(String originalEventId, String impactType, int sequence);

    List<PositionImpactEventDocument> findByTickerAndAssetTypeAndBrokerDocumentOrderByEventDateAscSequenceAsc(
            String ticker,
            String assetType,
            String brokerDocument);
}
