package com.investmentmanager.portfolioevent.adapter.out.persistence.broker;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface BrokerCatalogMongoRepository extends MongoRepository<BrokerCatalogDocument, String> {

    Optional<BrokerCatalogDocument> findByBrokerKey(String brokerKey);

    Optional<BrokerCatalogDocument> findFirstByNormalizedKnownDocumentsContains(String normalizedDocument);

    Optional<BrokerCatalogDocument> findFirstByNormalizedKnownNamesContains(String normalizedName);
}
