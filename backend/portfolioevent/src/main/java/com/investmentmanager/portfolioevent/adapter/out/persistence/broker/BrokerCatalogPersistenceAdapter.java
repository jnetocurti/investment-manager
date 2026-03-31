package com.investmentmanager.portfolioevent.adapter.out.persistence.broker;

import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerCatalogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class BrokerCatalogPersistenceAdapter implements BrokerCatalogRepositoryPort {

    private final BrokerCatalogMongoRepository repository;

    @Override
    public Optional<CanonicalBroker> findByNormalizedDocument(String normalizedDocument) {
        return repository.findFirstByNormalizedKnownDocumentsContains(normalizedDocument)
                .map(BrokerCatalogDocumentMapper::toDomain);
    }

    @Override
    public Optional<CanonicalBroker> findByNormalizedName(String normalizedName) {
        return repository.findFirstByNormalizedKnownNamesContains(normalizedName)
                .map(BrokerCatalogDocumentMapper::toDomain);
    }

    @Override
    public Optional<CanonicalBroker> findByBrokerKey(String brokerKey) {
        return repository.findByBrokerKey(brokerKey)
                .map(BrokerCatalogDocumentMapper::toDomain);
    }

    @Override
    public CanonicalBroker save(CanonicalBroker broker) {
        return BrokerCatalogDocumentMapper.toDomain(repository.save(BrokerCatalogDocumentMapper.toDocument(broker)));
    }
}
