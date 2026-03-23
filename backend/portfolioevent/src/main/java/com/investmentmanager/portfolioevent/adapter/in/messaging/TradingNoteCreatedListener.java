package com.investmentmanager.portfolioevent.adapter.in.messaging;

import com.investmentmanager.commons.domain.model.OperationType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventCreatedEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradingNoteCreatedListener {

    private final PortfolioEventPublisherPort eventPublisher;

    @RabbitListener(queues = "tradingnote.created.queue")
    public void onTradingNoteCreated(Map<String, Object> message) {
        log.info("Received TradingNoteCreatedEvent: {}", message);

        // TODO: substituir por lógica real de transformação
        var stubEvent = PortfolioEventCreatedEvent.builder()
                .portfolioEventId(UUID.randomUUID().toString())
                .ticker("PETR4")
                .type(OperationType.BUY)
                .quantity(100)
                .unitPrice(new BigDecimal("28.50"))
                .totalFees(new BigDecimal("5.00"))
                .eventDate(LocalDate.now())
                .build();

        eventPublisher.publishCreated(stubEvent);
        log.info("Published PortfolioEventCreatedEvent for ticker={}", stubEvent.getTicker());
    }
}
