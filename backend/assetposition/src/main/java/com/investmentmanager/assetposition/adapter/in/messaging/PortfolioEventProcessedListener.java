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

    @RabbitListener(queues = "portfolioevent.impact.queue")
    public void onPositionImpactCreated(PositionImpactCreatedMessage message) {
        log.info("Recebido impacto de posição: ticker={}, impactType={}, broker={} ({})",
                message.getTicker(), message.getImpactType(), message.getBrokerName(), message.getBrokerDocument());

        try {
            useCase.calculatePosition(message.getTicker(), message.getBrokerDocument());
        } catch (Exception e) {
            log.error("Erro ao calcular posição: asset={}, brokerDoc={}",
                    message.getTicker(), message.getBrokerDocument(), e);
            dlqPublisher.publishFailure(message.getTicker(), message.getBrokerDocument(),
                    "POSITION_IMPACT", message.getOriginalEventId(), e.getMessage());
        }
    }
}
