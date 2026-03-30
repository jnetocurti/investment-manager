package com.investmentmanager.portfolioevent.adapter.out.persistence.broker;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "brokers")
class BrokerRegistryDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String brokerKey;

    private String currentName;
    private String currentDocument;
    private List<String> knownNames;
    private List<String> knownDocuments;
    private LocalDateTime updatedAt;
}
