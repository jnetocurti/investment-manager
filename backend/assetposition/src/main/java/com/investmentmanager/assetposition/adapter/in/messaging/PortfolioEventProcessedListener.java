package com.investmentmanager.assetposition.adapter.in.messaging;

import com.investmentmanager.assetposition.adapter.out.messaging.AssetPositionDlqPublisher;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioEventProcessedListener {

    private final CalculateAssetPositionUseCase useCase;
    private final AssetPositionDlqPublisher dlqPublisher;

    @RabbitListener(queues = "portfolioevent.processed.queue")
    public void onPortfolioEventsProcessed(PortfolioEventsProcessedMessage message) {
        log.info("Recebido trigger de processamento: {} ativos, broker={} ({})",
                message.getAssetNames().size(), message.getBrokerName(), message.getBrokerDocument());

        for (String assetName : message.getAssetNames()) {
            try {
                useCase.calculatePosition(assetName, message.getBrokerDocument());
            } catch (Exception e) {
                log.error("Erro ao calcular posição: asset={}, brokerDoc={}",
                        assetName, message.getBrokerDocument(), e);
                dlqPublisher.publishFailure(assetName, message.getBrokerDocument(),
                        message.getSourceType(), message.getSourceReferenceId(),
                        e.getMessage());
            }
        }
    }
}
