package com.investmentmanager.assetposition.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface BrokerRegistryMongoRepository extends MongoRepository<BrokerRegistryDocument, String> {

    Optional<BrokerRegistryDocument> findByBrokerKey(String brokerKey);
}
