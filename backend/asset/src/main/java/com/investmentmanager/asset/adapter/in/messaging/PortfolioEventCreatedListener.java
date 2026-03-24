package com.investmentmanager.asset.adapter.in.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PortfolioEventCreatedListener {

    @RabbitListener(queues = "portfolioevent.created.queue")
    public void onPortfolioEventCreated(List<Map<String, Object>> events) {
        log.info("Received {} PortfolioEventCreatedEvents", events.size());
        // TODO: chamar use case de criação/atualização de asset
        events.forEach(event -> log.info("  Event: type={}, asset={}", event.get("eventType"), event.get("assetName")));
    }
}
