package com.investmentmanager.portfolioevent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PortfolioEventMongoRepository extends MongoRepository<PortfolioEventDocument, String> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
