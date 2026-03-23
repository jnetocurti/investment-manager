package com.investmentmanager.tradingnote.adapter.out.messaging;

import com.investmentmanager.tradingnote.domain.model.TradingNoteCreatedEvent;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradingNoteRabbitPublisher implements TradingNoteEventPublisherPort {

    private static final String EXCHANGE = "tradingnote.exchange";
    private static final String ROUTING_KEY = "tradingnote.created";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishCreated(TradingNoteCreatedEvent event) {
        log.debug("Publishing TradingNoteCreatedEvent for noteId={}", event.getTradingNoteId());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
