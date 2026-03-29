package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.portfolioevent.domain.exception.IdempotentOperationException;
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

        var saved = persistWithIdempotencyTranslation(documents);

        return saved.stream()
                .map(PortfolioEventDocumentMapper::toDomain)
                .toList();
    }

    private List<PortfolioEventDocument> persistWithIdempotencyTranslation(List<PortfolioEventDocument> documents) {
        try {
            return mongoRepository.saveAll(documents);
        } catch (DuplicateKeyException ex) {
            throw new IdempotentOperationException("Evento de portfólio já registrado para a mesma chave de idempotência");
        }
    }

    @Override
    public boolean existsBySourceReferenceId(String sourceReferenceId) {
        return mongoRepository.existsBySourceReferenceId(sourceReferenceId);
    }

    @Override
    public boolean existsSubscriptionByUniqueKey(
            String eventType,
            String assetName,
            String assetType,
            String brokerDocument,
            LocalDate eventDate) {
        return mongoRepository.existsByEventTypeAndAssetNameAndAssetTypeAndBrokerDocumentAndEventDate(
                eventType,
                assetName,
                assetType,
                brokerDocument,
                eventDate);
    }

    @Override
    public Optional<PortfolioEvent> findById(String id) {
        return mongoRepository.findById(id)
                .map(PortfolioEventDocumentMapper::toDomain);
    }
}
