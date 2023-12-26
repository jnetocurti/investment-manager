package br.com.investmentmanager.tradingnote.domain.events;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import br.com.investmentmanager.tradingnote.domain.valueobjects.TradingNoteFile;
import lombok.NonNull;

public class TradingNoteFileCreated extends DomainEvent<TradingNoteFile> {

    public TradingNoteFileCreated(@NonNull TradingNoteFile source) {
        super(source);
    }
}
