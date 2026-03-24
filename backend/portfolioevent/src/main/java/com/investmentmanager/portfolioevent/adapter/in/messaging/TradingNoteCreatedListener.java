package com.investmentmanager.portfolioevent.adapter.in.messaging;

import com.investmentmanager.commons.domain.model.OperationType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsCommand;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradingNoteCreatedListener {

    private final CreatePortfolioEventsUseCase useCase;

    @RabbitListener(queues = "tradingnote.created.queue")
    public void onTradingNoteCreated(TradingNoteMessage message) {
        log.info("Recebido TradingNoteCreatedEvent: noteId={}", message.getTradingNoteId());

        try {
            var command = CreatePortfolioEventsCommand.builder()
                    .tradingNoteId(message.getTradingNoteId())
                    .noteNumber(message.getNoteNumber())
                    .brokerName(message.getBrokerName())
                    .tradingDate(message.getTradingDate())
                    .currency(message.getCurrency())
                    .operations(message.getOperations().stream()
                            .map(op -> CreatePortfolioEventsCommand.OperationData.builder()
                                    .assetName(op.getAssetName())
                                    .operationType(OperationType.valueOf(op.getOperationType()))
                                    .quantity(op.getQuantity())
                                    .unitPrice(op.getUnitPrice())
                                    .totalValue(op.getTotalValue())
                                    .fee(op.getFee())
                                    .build())
                            .toList())
                    .build();

            List<PortfolioEvent> events = useCase.createFromTradingNote(command);
            log.info("Criados {} eventos de portfólio para noteId={}", events.size(), message.getTradingNoteId());

        } catch (Exception e) {
            log.error("Erro ao processar TradingNoteCreatedEvent noteId={}: {}",
                    message.getTradingNoteId(), e.getMessage(), e);
        }
    }
}
