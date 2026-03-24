package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventCreatedEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsCommand;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsUseCase;
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
 *   <li>Publicação em batch — uma mensagem com todos os eventos</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class PortfolioEventService implements CreatePortfolioEventsUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PortfolioEventPublisherPort publisher;

    @Override
    public List<PortfolioEvent> createFromTradingNote(CreatePortfolioEventsCommand command) {
        validate(command);

        // Idempotência: nota já processada?
        if (repository.existsBySourceReferenceId(command.getTradingNoteId())) {
            log.info("TradingNote {} já processada, ignorando", command.getTradingNoteId());
            return Collections.emptyList();
        }

        // Fase 1: mapeamento puro — se qualquer operação falhar na validação, nenhum evento é criado
        List<PortfolioEvent> events = command.getOperations().stream()
                .map(op -> PortfolioEvent.fromOperation(
                        command.getTradingNoteId(),
                        command.getBrokerName(),
                        command.getTradingDate(),
                        op.getAssetName(),
                        op.getOperationType(),
                        op.getQuantity(),
                        op.getUnitPrice(),
                        op.getTotalValue(),
                        op.getFee(),
                        command.getCurrency()))
                .toList();

        // Fase 2: persistência em bulk
        List<PortfolioEvent> savedEvents = repository.saveAll(events);

        // Fase 3: publicação em batch
        List<PortfolioEventCreatedEvent> createdEvents = savedEvents.stream()
                .map(PortfolioEventCreatedEvent::from)
                .toList();
        publisher.publishAllCreated(createdEvents);

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
