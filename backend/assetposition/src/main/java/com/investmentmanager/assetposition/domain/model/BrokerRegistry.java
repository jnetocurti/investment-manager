package com.investmentmanager.assetposition.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class BrokerRegistry {

    private final String id;
    private final String brokerKey;
    private final String currentName;
    private final String currentDocument;
    private final List<String> knownNames;
    private final List<String> knownDocuments;
    private final LocalDateTime updatedAt;
}
