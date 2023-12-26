package br.com.investmentmanager.tradingnote.domain.repository;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;

public interface TradingNoteRepository {

    void save(TradingNote tradingNote);
}
