package br.com.investmentmanager.asset.application.amqp;

import br.com.investmentmanager.asset.application.input.PortfolioEventMessage;
import br.com.investmentmanager.asset.application.usecases.AddAssetPurchase;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssetListener {

    private final AddAssetPurchase addAssetPurchase;

    @RabbitListener(queues = "${portfolioevent.infrastructure.rabbitmq.portfolio-event-created-queue}")
    public void handle(@NonNull PortfolioEventMessage event) {
        if (Operation.BUY.equals(event.getOperation())) {
            addAssetPurchase.execute(event);
        }
    }
}
