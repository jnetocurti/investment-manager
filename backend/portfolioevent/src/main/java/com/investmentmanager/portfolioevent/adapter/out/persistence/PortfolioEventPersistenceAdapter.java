package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class PortfolioEventPersistenceAdapter implements PortfolioEventRepositoryPort {

    private final PortfolioEventMongoRepository mongoRepository;

    @Override
    public List<PortfolioEvent> saveAll(List<PortfolioEvent> portfolioEvents) {
        var documents = portfolioEvents.stream()
                .map(PortfolioEventDocumentMapper::toDocument)
                .toList();

        try {
            var saved = mongoRepository.saveAll(documents);

            return saved.stream()
                    .map(PortfolioEventDocumentMapper::toDomain)
                    .toList();
        } catch (DuplicateKeyException e) {
            boolean hasSubscription = portfolioEvents.stream()
                    .anyMatch(event -> "SUBSCRIPTION".equals(event.getEventType().name()));
            if (hasSubscription) {
                throw new IllegalStateException("Subscrição duplicada para a mesma posição e data", e);
            }
            throw e;
        }
    }

    @Override
    public boolean existsBySourceReferenceId(String sourceReferenceId) {
        return mongoRepository.existsBySourceReferenceId(sourceReferenceId);
    }

    @Override
    public boolean existsSubscriptionByBusinessKey(
            String assetName,
            AssetType assetType,
            String brokerKey,
            LocalDate eventDate) {
        return mongoRepository.existsByEventTypeAndAssetNameAndAssetTypeAndBrokerKeyAndEventDate(
                "SUBSCRIPTION",
                assetName,
                assetType != null ? assetType.name() : null,
                brokerKey,
                eventDate);
    }

    @Override
    public Optional<PortfolioEvent> findById(String id) {
        return mongoRepository.findById(id)
                .map(PortfolioEventDocumentMapper::toDomain);
    }
}
