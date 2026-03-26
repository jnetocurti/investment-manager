package com.investmentmanager.assetposition.adapter.in.messaging;

import com.investmentmanager.assetposition.adapter.out.messaging.AssetPositionDlqPublisher;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import com.investmentmanager.commons.domain.model.AssetType;
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
                message.getAssetKeys().size(), message.getBrokerName(), message.getBrokerDocument());

        for (PortfolioEventsProcessedMessage.AssetKeyMessage assetKey : message.getAssetKeys()) {
            String assetName = assetKey.getAssetName();
            try {
                AssetType assetType = assetKey.getAssetType() != null
                        ? AssetType.valueOf(assetKey.getAssetType())
                        : null;
                useCase.calculatePosition(assetName, assetType, message.getBrokerDocument());
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
