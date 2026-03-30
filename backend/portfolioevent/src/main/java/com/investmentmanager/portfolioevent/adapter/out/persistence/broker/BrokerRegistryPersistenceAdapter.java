package com.investmentmanager.portfolioevent.adapter.out.persistence.broker;

import com.investmentmanager.commons.domain.model.BrokerIdentityResolver;
import com.investmentmanager.portfolioevent.domain.model.BrokerRecord;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerRegistryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@Component
@RequiredArgsConstructor
class BrokerRegistryPersistenceAdapter implements BrokerRegistryPort {

    private final BrokerRegistryMongoRepository repository;

    @Override
    public BrokerRecord resolveOrCreate(String brokerName, String brokerDocument) {
        var identity = BrokerIdentityResolver.resolve(brokerName, brokerDocument);
        return repository.findByBrokerKey(identity.getBrokerKey())
                .map(existing -> saveMerged(existing, brokerName, brokerDocument, identity))
                .orElseGet(() -> createNew(brokerName, brokerDocument, identity));
    }

    private BrokerRecord saveMerged(BrokerRegistryDocument doc,
                                    String brokerName,
                                    String brokerDocument,
                                    BrokerIdentityResolver.BrokerIdentity identity) {
        LinkedHashSet<String> names = new LinkedHashSet<>(doc.getKnownNames() != null ? doc.getKnownNames() : new ArrayList<>());
        LinkedHashSet<String> docs = new LinkedHashSet<>(doc.getKnownDocuments() != null ? doc.getKnownDocuments() : new ArrayList<>());
        names.addAll(identity.getKnownNames());
        docs.addAll(identity.getKnownDocuments());
        if (brokerName != null && !brokerName.isBlank()) names.add(brokerName);
        if (brokerDocument != null && !brokerDocument.isBlank()) docs.add(brokerDocument);

        doc.setCurrentName(brokerName);
        doc.setCurrentDocument(brokerDocument);
        doc.setKnownNames(new ArrayList<>(names));
        doc.setKnownDocuments(new ArrayList<>(docs));
        doc.setUpdatedAt(LocalDateTime.now());

        var saved = repository.save(doc);
        return toRecord(saved);
    }

    private BrokerRecord createNew(String brokerName,
                                   String brokerDocument,
                                   BrokerIdentityResolver.BrokerIdentity identity) {
        var doc = new BrokerRegistryDocument();
        doc.setBrokerKey(identity.getBrokerKey());
        doc.setCurrentName(brokerName);
        doc.setCurrentDocument(brokerDocument);
        doc.setKnownNames(new ArrayList<>(identity.getKnownNames()));
        doc.setKnownDocuments(new ArrayList<>(identity.getKnownDocuments()));
        doc.setUpdatedAt(LocalDateTime.now());

        try {
            return toRecord(repository.save(doc));
        } catch (DuplicateKeyException e) {
            return repository.findByBrokerKey(identity.getBrokerKey())
                    .map(BrokerRegistryPersistenceAdapter::toRecord)
                    .orElseThrow(() -> e);
        }
    }

    private static BrokerRecord toRecord(BrokerRegistryDocument doc) {
        return BrokerRecord.builder()
                .id(doc.getId())
                .brokerKey(doc.getBrokerKey())
                .currentName(doc.getCurrentName())
                .currentDocument(doc.getCurrentDocument())
                .build();
    }
}
