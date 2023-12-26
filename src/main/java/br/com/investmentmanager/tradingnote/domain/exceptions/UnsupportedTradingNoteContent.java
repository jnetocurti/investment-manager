package br.com.investmentmanager.tradingnote.domain.exceptions;

public class UnsupportedTradingNoteContent extends RuntimeException {

    public UnsupportedTradingNoteContent() {
        super("Unsupported trading note content");
    }
}
