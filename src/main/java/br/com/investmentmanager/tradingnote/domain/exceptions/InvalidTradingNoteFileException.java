package br.com.investmentmanager.tradingnote.domain.exceptions;

import lombok.NonNull;

public class InvalidTradingNoteFileException extends RuntimeException {

    public InvalidTradingNoteFileException(@NonNull Throwable cause) {
        super("Invalid trading note file", cause);
    }
}
