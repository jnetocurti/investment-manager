package br.com.investmentmanager.tradingnote.domain;

import lombok.NonNull;

public interface TradingNoteRepository {

    void save(@NonNull TradingNote tradingNote);
}
