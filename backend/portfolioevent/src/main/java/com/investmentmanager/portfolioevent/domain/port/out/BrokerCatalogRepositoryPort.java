package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;

import java.util.Optional;

public interface BrokerCatalogRepositoryPort {

    Optional<CanonicalBroker> findByNormalizedDocument(String normalizedDocument);

    Optional<CanonicalBroker> findByNormalizedName(String normalizedName);

    Optional<CanonicalBroker> findByBrokerKey(String brokerKey);

    CanonicalBroker save(CanonicalBroker broker);
}
