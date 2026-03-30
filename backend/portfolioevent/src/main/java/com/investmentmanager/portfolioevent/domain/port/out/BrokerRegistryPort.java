package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.BrokerRecord;

public interface BrokerRegistryPort {
    BrokerRecord resolveOrCreate(String brokerName, String brokerDocument);
}
