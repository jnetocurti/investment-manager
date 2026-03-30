package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsCommand;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsUseCase;
import com.investmentmanager.portfolioevent.domain.port.out.AssetDetailResolverPort;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerRegistryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PortfolioEventService implements CreatePortfolioEventsUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final AssetDetailResolverPort assetDetailResolver;
    private final PositionImpactGenerationService impactGenerationService;
    private final BrokerRegistryPort brokerRegistryPort;

    @Override
    public List<PortfolioEvent> createFromTradingNote(CreatePortfolioEventsCommand command) {
        validate(command);

        if (repository.existsBySourceReferenceId(command.getTradingNoteId())) {
            log.info("TradingNote {} já processada, ignorando", command.getTradingNoteId());
            return Collections.emptyList();
        }

        var broker = brokerRegistryPort.resolveOrCreate(command.getBrokerName(), command.getBrokerDocument());

        List<PortfolioEvent> events = command.getOperations().stream()
                .map(op -> {
                    var detail = assetDetailResolver.resolve(op.getAssetDescription());
                    return PortfolioEvent.fromOperation(
                            command.getTradingNoteId(),
                            broker.getId(),
                            command.getTradingDate(),
                            detail.ticker(),
                            detail.assetType(),
                            op.getOperationType(),
                            op.getQuantity(),
                            op.getUnitPrice(),
                            op.getTotalValue(),
                            op.getFee(),
                            command.getCurrency());
                })
                .toList();

        List<PortfolioEvent> savedEvents = repository.saveAll(events);
        List<PositionImpactEvent> persistedImpacts = impactGenerationService.generateAndPublish(savedEvents);

        log.info("Criados {} eventos de portfólio e {} impactos para noteId={}",
                savedEvents.size(), persistedImpacts.size(), command.getTradingNoteId());
        return savedEvents;
    }

    private void validate(CreatePortfolioEventsCommand command) {
        if (command == null)
            throw new IllegalArgumentException("Command is required");
        if (command.getTradingNoteId() == null || command.getTradingNoteId().isBlank())
            throw new IllegalArgumentException("Trading note ID is required");
        if (command.getOperations() == null || command.getOperations().isEmpty())
            throw new IllegalArgumentException("At least one operation is required");
    }
}
