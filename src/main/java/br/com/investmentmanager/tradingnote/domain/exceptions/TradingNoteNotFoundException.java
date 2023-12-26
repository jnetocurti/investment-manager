package br.com.investmentmanager.tradingnote.domain.exceptions;

import br.com.investmentmanager.shared.domain.exceptions.EntityNotFoundException;

public class TradingNoteNotFoundException extends EntityNotFoundException {

    public TradingNoteNotFoundException() {
        super("Trading note not found");
    }
}
