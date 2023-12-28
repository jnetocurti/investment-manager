package br.com.investmentmanager.tradingnote.domain.events.handler;

import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TradingNoteCreatedHandler {

    @EventListener
    public void onMyDomainEvent(TradingNoteCreated event) {
        System.out.println(event);
    }

}
