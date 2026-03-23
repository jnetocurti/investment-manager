package com.investmentmanager.asset.adapter.in.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PortfolioEventCreatedListener {

    @RabbitListener(queues = "portfolioevent.created.queue")
    public void onPortfolioEventCreated(Map<String, Object> message) {
        log.info("Received PortfolioEventCreatedEvent: {}", message);
        // TODO: chamar use case de criação/atualização de asset
        log.info("Asset module received event — end of pipeline");
    }
}
