package com.investmentmanager.portfolioevent.adapter.out.persistence.broker;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface BrokerRegistryMongoRepository extends MongoRepository<BrokerRegistryDocument, String> {
    Optional<BrokerRegistryDocument> findByBrokerKey(String brokerKey);
}
