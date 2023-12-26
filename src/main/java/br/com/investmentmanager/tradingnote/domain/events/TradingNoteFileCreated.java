package br.com.investmentmanager.tradingnote.domain.events;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNoteFile;

public class TradingNoteFileCreated extends DomainEvent<TradingNoteFile> {

    public TradingNoteFileCreated(TradingNoteFile source) {
        super(source);
    }
}
