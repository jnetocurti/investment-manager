package com.investmentmanager.portfolioevent.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrokerRecord {
    private final String id;
    private final String brokerKey;
    private final String currentName;
    private final String currentDocument;
}
