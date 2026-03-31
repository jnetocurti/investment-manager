package com.investmentmanager.portfolioevent.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Set;

@Value
@Builder(toBuilder = true)
public class CanonicalBroker {

    String id;
    String brokerKey;
    String currentName;
    String currentDocument;
    Set<String> knownNames;
    Set<String> knownDocuments;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
