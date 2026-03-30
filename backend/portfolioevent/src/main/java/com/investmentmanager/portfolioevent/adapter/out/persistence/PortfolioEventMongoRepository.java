package com.investmentmanager.portfolioevent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PortfolioEventMongoRepository extends MongoRepository<PortfolioEventDocument, String> {

    boolean existsBySourceReferenceId(String sourceReferenceId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<PortfolioEventDocument> findByAssetNameAndBrokerIdOrderByEventDateAsc(String assetName, String brokerId);
}
