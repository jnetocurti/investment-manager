package com.investmentmanager.portfolioevent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioEventMongoRepository extends MongoRepository<PortfolioEventDocument, String> {

    boolean existsBySourceReferenceId(String sourceReferenceId);

    boolean existsByEventTypeAndAssetNameAndAssetTypeAndBrokerKeyAndEventDate(
            String eventType,
            String assetName,
            String assetType,
            String brokerKey,
            LocalDate eventDate);

    List<PortfolioEventDocument> findByAssetNameAndBrokerNameOrderByEventDateAsc(
            String assetName, String brokerName);

    List<PortfolioEventDocument> findByAssetNameAndBrokerDocumentOrderByEventDateAsc(
            String assetName, String brokerDocument);
}
