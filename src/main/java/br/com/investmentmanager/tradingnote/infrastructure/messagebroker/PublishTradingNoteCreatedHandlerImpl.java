package br.com.investmentmanager.tradingnote.infrastructure.messagebroker;

import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import br.com.investmentmanager.tradingnote.domain.events.handler.PublishTradingNoteCreatedHandler;
import br.com.investmentmanager.tradingnote.infrastructure.messagebroker.model.TradingNoteMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishTradingNoteCreatedHandlerImpl implements PublishTradingNoteCreatedHandler {

    private final RabbitTemplate rabbitTemplate;

    @Value("${tradingnote.infrastructure.rabbitmq.trading-note-exchange}")
    private String exchange;
    @Value("${tradingnote.infrastructure.rabbitmq.trading-note-created-routing-key}")
    private String createdRoutingKey;

    @Override
    public void handle(@NonNull TradingNoteCreated event) {
        rabbitTemplate.convertAndSend(exchange, createdRoutingKey, new ModelMapper().map(event.getSource(),
                TradingNoteMessage.class));
    }
}
