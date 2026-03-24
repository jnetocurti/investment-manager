package com.investmentmanager.portfolioevent.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

interface PortfolioEventMongoRepository extends MongoRepository<PortfolioEventDocument, String> {

    boolean existsBySourceReferenceId(String sourceReferenceId);
}
