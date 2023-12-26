package br.com.investmentmanager.tradingnote.domain.exceptions;

import br.com.investmentmanager.shared.domain.exceptions.EntityAlreadyExistsException;
import lombok.NonNull;

import java.util.UUID;

public class TradingNoteAlreadyExistsException extends EntityAlreadyExistsException {

    public TradingNoteAlreadyExistsException(@NonNull UUID entityId) {
        super("The trading note already exists", entityId);
    }
}
