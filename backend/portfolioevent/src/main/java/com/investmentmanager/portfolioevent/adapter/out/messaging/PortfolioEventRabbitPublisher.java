package com.investmentmanager.portfolioevent.adapter.out.messaging;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEventsProcessedEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioEventRabbitPublisher implements PortfolioEventPublisherPort {

    private static final String EXCHANGE = "portfolioevent.exchange";
    private static final String ROUTING_KEY = "portfolioevent.processed";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishProcessed(PortfolioEventsProcessedEvent event) {
        log.info("Publicando notificação para {} ativos afetados", event.getAssetNames().size());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
