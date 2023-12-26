package br.com.investmentmanager.tradingnote.domain.events;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;

public class TradingNoteCreated extends DomainEvent<TradingNote> {

    public TradingNoteCreated(TradingNote source) {
        super(source);
    }
}
