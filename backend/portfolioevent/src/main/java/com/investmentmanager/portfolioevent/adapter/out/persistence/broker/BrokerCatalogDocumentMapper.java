package com.investmentmanager.portfolioevent.adapter.out.persistence.broker;

import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BrokerCatalogDocumentMapper {

    static BrokerCatalogDocument toDocument(CanonicalBroker broker) {
        BrokerCatalogDocument doc = new BrokerCatalogDocument();
        doc.setId(broker.getId());
        doc.setBrokerKey(broker.getBrokerKey());
        doc.setCurrentName(broker.getCurrentName());
        doc.setCurrentDocument(broker.getCurrentDocument());
        doc.setKnownNames(broker.getKnownNames());
        doc.setKnownDocuments(broker.getKnownDocuments());
        doc.setNormalizedCurrentName(normalizeName(broker.getCurrentName()));
        doc.setNormalizedCurrentDocument(normalizeDocument(broker.getCurrentDocument()));
        doc.setNormalizedKnownNames(normalizeNames(broker.getKnownNames()));
        doc.setNormalizedKnownDocuments(normalizeDocuments(broker.getKnownDocuments()));
        doc.setCreatedAt(broker.getCreatedAt());
        doc.setUpdatedAt(broker.getUpdatedAt());
        return doc;
    }

    static CanonicalBroker toDomain(BrokerCatalogDocument doc) {
        return CanonicalBroker.builder()
                .id(doc.getId())
                .brokerKey(doc.getBrokerKey())
                .currentName(doc.getCurrentName())
                .currentDocument(doc.getCurrentDocument())
                .knownNames(doc.getKnownNames() != null ? doc.getKnownNames() : Collections.emptySet())
                .knownDocuments(doc.getKnownDocuments() != null ? doc.getKnownDocuments() : Collections.emptySet())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private static Set<String> normalizeNames(Set<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream().map(BrokerCatalogDocumentMapper::normalizeName).collect(Collectors.toSet());
    }

    private static Set<String> normalizeDocuments(Set<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream().map(BrokerCatalogDocumentMapper::normalizeDocument).collect(Collectors.toSet());
    }

    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "").trim();
    }

    private static String normalizeDocument(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "").trim();
    }
}
