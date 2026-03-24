package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventsProcessedEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsCommand;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsUseCase;
import com.investmentmanager.portfolioevent.domain.port.out.AssetDetailResolverPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Orquestra a criação de eventos de portfólio a partir de notas de negociação.
 *
 * <p>Executa em 3 fases atômicas:</p>
 * <ol>
 *   <li>Mapeamento puro (sem I/O) — todas as operações viram eventos</li>
 *   <li>Persistência em bulk — todos ou nenhum</li>
 *   <li>Publicação de trigger — notifica quais ativos foram afetados</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class PortfolioEventService implements CreatePortfolioEventsUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PortfolioEventPublisherPort publisher;
    private final AssetDetailResolverPort assetDetailResolver;

    @Override
    public List<PortfolioEvent> createFromTradingNote(CreatePortfolioEventsCommand command) {
        validate(command);

        if (repository.existsBySourceReferenceId(command.getTradingNoteId())) {
            log.info("TradingNote {} já processada, ignorando", command.getTradingNoteId());
            return Collections.emptyList();
        }

        // Fase 1: mapeamento puro
        List<PortfolioEvent> events = command.getOperations().stream()
                .map(op -> {
                    var detail = assetDetailResolver.resolve(op.getAssetName());
                    return PortfolioEvent.fromOperation(
                        command.getTradingNoteId(),
                        command.getBrokerName(),
                        command.getBrokerDocument(),
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

        // Fase 2: persistência em bulk
        List<PortfolioEvent> savedEvents = repository.saveAll(events);

        // Fase 3: publicação de trigger simplificado
        List<String> assetNames = savedEvents.stream()
                .map(PortfolioEvent::getAssetName)
                .distinct()
                .toList();

        publisher.publishProcessed(PortfolioEventsProcessedEvent.builder()
                .assetNames(assetNames)
                .brokerName(command.getBrokerName())
                .brokerDocument(command.getBrokerDocument())
                .sourceType("TRADING_NOTE")
                .sourceReferenceId(command.getTradingNoteId())
                .build());

        log.info("Criados {} eventos de portfólio para noteId={}", savedEvents.size(), command.getTradingNoteId());
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
