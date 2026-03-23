package com.investmentmanager.tradingnote.domain.port.out;

import com.investmentmanager.tradingnote.domain.model.TradingNote;

import java.util.Optional;

public interface TradingNoteRepositoryPort {

    TradingNote save(TradingNote tradingNote);

    boolean existsByFileHash(String fileHash);

    Optional<TradingNote> findByFileHash(String fileHash);
}
