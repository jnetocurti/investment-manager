package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.BrokerRegistry;

import java.util.Optional;

public interface BrokerRegistryRepositoryPort {

    Optional<BrokerRegistry> findByBrokerKey(String brokerKey);

    Optional<BrokerRegistry> findById(String brokerId);

    BrokerRegistry save(BrokerRegistry brokerRegistry);
}
