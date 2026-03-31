package com.investmentmanager.assetposition.domain.port.out;

import java.util.Optional;

public interface BrokerCatalogQueryPort {

    Optional<BrokerDisplayData> findByBrokerKey(String brokerKey);

    record BrokerDisplayData(String brokerName, String brokerDocument) {}
}
