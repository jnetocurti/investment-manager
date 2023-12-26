package br.com.investmentmanager.tradingnote.domain.events;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import lombok.NonNull;

public class TradingNoteCreated extends DomainEvent<TradingNote> {

    public TradingNoteCreated(@NonNull TradingNote source) {
        super(source);
    }
}
