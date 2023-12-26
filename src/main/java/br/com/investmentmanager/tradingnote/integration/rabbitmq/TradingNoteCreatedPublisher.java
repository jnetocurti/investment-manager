package br.com.investmentmanager.tradingnote.integration.rabbitmq;

import br.com.investmentmanager.shared.integration.EventPublisher;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import org.springframework.stereotype.Component;

@Component
public class TradingNoteCreatedPublisher implements EventPublisher<TradingNoteCreated> {

    @Override
    public void publish(TradingNoteCreated event) {
    }
}
