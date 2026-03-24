package com.investmentmanager.assetposition.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetPositionDlqPublisher {

    private static final String EXCHANGE = "assetposition.exchange";
    private static final String ROUTING_KEY = "assetposition.calculated.dlq";

    private final RabbitTemplate rabbitTemplate;

    public void publishFailure(String assetName, String brokerName,
                               String sourceType, String sourceReferenceId,
                               String error) {
        var message = Map.of(
                "assetName", assetName,
                "brokerName", brokerName,
                "sourceType", sourceType,
                "sourceReferenceId", sourceReferenceId,
                "error", error,
                "failedAt", LocalDateTime.now().toString()
        );
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        log.warn("Publicado na DLQ: asset={}, broker={}, error={}", assetName, brokerName, error);
    }
}
