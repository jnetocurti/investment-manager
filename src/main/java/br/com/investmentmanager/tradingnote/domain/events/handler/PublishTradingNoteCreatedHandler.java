package br.com.investmentmanager.tradingnote.domain.events.handler;

import br.com.investmentmanager.shared.integration.EventPublisher;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishTradingNoteCreatedHandler {

    private final EventPublisher<TradingNoteCreated> publisher;

    @EventListener
    public void handleTradingNoteCreated(TradingNoteCreated event) {
        publisher.publish(event);
    }
}
