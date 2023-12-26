package br.com.investmentmanager.tradingnote.domain.exceptions;

public class UnsupportedTradingNoteContentException extends RuntimeException {

    public UnsupportedTradingNoteContentException() {
        super("Unsupported trading note content");
    }
}
