package br.com.investmentmanager.portfolioevent.application.amqp;

import br.com.investmentmanager.portfolioevent.application.input.TradingNoteMessage;
import br.com.investmentmanager.portfolioevent.application.usecases.CreateFromTradingNote;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradingNoteListener {

    private final CreateFromTradingNote createFromTradingNote;

    @RabbitListener(queues = "${tradingnote.infrastructure.rabbitmq.trading-note-created-queue}")
    public void handle(@NonNull TradingNoteMessage event) {
        createFromTradingNote.execute(event);
    }
}
