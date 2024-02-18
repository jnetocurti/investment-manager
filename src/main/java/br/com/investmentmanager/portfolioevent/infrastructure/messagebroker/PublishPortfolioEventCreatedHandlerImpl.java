package br.com.investmentmanager.portfolioevent.infrastructure.messagebroker;

import br.com.investmentmanager.portfolioevent.domain.events.PortfolioEventCreated;
import br.com.investmentmanager.portfolioevent.domain.events.handler.PublishPortfolioEventCreatedHandler;
import br.com.investmentmanager.portfolioevent.infrastructure.messagebroker.model.PortfolioEventMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishPortfolioEventCreatedHandlerImpl implements PublishPortfolioEventCreatedHandler {

    private final RabbitTemplate rabbitTemplate;

    @Value("${portfolioevent.infrastructure.rabbitmq.portfolio-event-exchange}")
    private String exchange;
    @Value("${portfolioevent.infrastructure.rabbitmq.portfolio-event-created-routing-key}")
    private String createdRoutingKey;

    @Override
    public void handle(@NonNull PortfolioEventCreated event) {
        rabbitTemplate.convertAndSend(exchange, createdRoutingKey, new ModelMapper().map(event.getSource(),
                PortfolioEventMessage.class));
    }
}
