package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

        var saved = mongoRepository.saveAll(documents);

        return saved.stream()
                .map(PortfolioEventDocumentMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySourceReferenceId(String sourceReferenceId) {
        return mongoRepository.existsBySourceReferenceId(sourceReferenceId);
    }

    @Override
    public Optional<PortfolioEvent> findById(String id) {
        return mongoRepository.findById(id)
                .map(PortfolioEventDocumentMapper::toDomain);
    }
}
