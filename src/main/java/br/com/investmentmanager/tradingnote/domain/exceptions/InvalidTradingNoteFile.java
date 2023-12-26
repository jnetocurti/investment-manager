package br.com.investmentmanager.tradingnote.domain.exceptions;

public class InvalidTradingNoteFile extends RuntimeException {

    public InvalidTradingNoteFile(Throwable cause) {
        super("Invalid trading note file", cause);
    }
}
