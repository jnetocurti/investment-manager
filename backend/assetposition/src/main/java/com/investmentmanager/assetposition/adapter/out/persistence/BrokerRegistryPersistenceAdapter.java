package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.BrokerRegistry;
import com.investmentmanager.assetposition.domain.port.out.BrokerRegistryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class BrokerRegistryPersistenceAdapter implements BrokerRegistryRepositoryPort {

    private final BrokerRegistryMongoRepository repository;

    @Override
    public Optional<BrokerRegistry> findByBrokerKey(String brokerKey) {
        return repository.findByBrokerKey(brokerKey).map(BrokerRegistryDocumentMapper::toDomain);
    }

    @Override
    public Optional<BrokerRegistry> findById(String brokerId) {
        return repository.findById(brokerId).map(BrokerRegistryDocumentMapper::toDomain);
    }

    @Override
    public BrokerRegistry save(BrokerRegistry brokerRegistry) {
        var saved = repository.save(BrokerRegistryDocumentMapper.toDocument(brokerRegistry));
        return BrokerRegistryDocumentMapper.toDomain(saved);
    }
}
