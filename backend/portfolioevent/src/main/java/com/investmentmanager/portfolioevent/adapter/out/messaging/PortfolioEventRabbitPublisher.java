package com.investmentmanager.portfolioevent.adapter.out.messaging;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEventCreatedEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioEventRabbitPublisher implements PortfolioEventPublisherPort {

    private static final String EXCHANGE = "portfolioevent.exchange";
    private static final String ROUTING_KEY = "portfolioevent.created";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishAllCreated(List<PortfolioEventCreatedEvent> events) {
        log.info("Publicando batch de {} eventos de portfólio", events.size());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, events);
    }
}
