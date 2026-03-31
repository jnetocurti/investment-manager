package com.investmentmanager.portfolioevent.adapter.out.persistence.broker;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Document(collection = "broker_catalog")
class BrokerCatalogDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String brokerKey;

    private String currentName;
    private String currentDocument;
    private Set<String> knownNames;
    private Set<String> knownDocuments;
    private String normalizedCurrentName;
    private String normalizedCurrentDocument;
    private Set<String> normalizedKnownNames;
    private Set<String> normalizedKnownDocuments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
