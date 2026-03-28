package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.BrokerRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BrokerRegistryDocumentMapper {

    static BrokerRegistryDocument toDocument(BrokerRegistry model) {
        var doc = new BrokerRegistryDocument();
        doc.setId(model.getId());
        doc.setBrokerKey(model.getBrokerKey());
        doc.setCurrentName(model.getCurrentName());
        doc.setCurrentDocument(model.getCurrentDocument());
        doc.setKnownNames(model.getKnownNames());
        doc.setKnownDocuments(model.getKnownDocuments());
        doc.setUpdatedAt(model.getUpdatedAt());
        return doc;
    }

    static BrokerRegistry toDomain(BrokerRegistryDocument doc) {
        return BrokerRegistry.builder()
                .id(doc.getId())
                .brokerKey(doc.getBrokerKey())
                .currentName(doc.getCurrentName())
                .currentDocument(doc.getCurrentDocument())
                .knownNames(doc.getKnownNames() != null ? doc.getKnownNames() : Collections.emptyList())
                .knownDocuments(doc.getKnownDocuments() != null ? doc.getKnownDocuments() : Collections.emptyList())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
