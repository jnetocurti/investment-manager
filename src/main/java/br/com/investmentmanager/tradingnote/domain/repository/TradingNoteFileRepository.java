package br.com.investmentmanager.tradingnote.domain.repository;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNoteFile;

public interface TradingNoteFileRepository {

    void save(TradingNoteFile file);
}
