package com.investmentmanager.portfolioevent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PortfolioEventMongoRepository extends MongoRepository<PortfolioEventDocument, String> {

    boolean existsBySourceReferenceId(String sourceReferenceId);

    List<PortfolioEventDocument> findByAssetNameAndBrokerNameOrderByEventDateAsc(
            String assetName, String brokerName);

    List<PortfolioEventDocument> findByAssetNameAndBrokerDocumentOrderByEventDateAsc(
            String assetName, String brokerDocument);
}
