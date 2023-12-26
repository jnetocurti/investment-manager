package br.com.investmentmanager.tradingnote.domain;

import lombok.NonNull;

import java.util.UUID;

public interface TradingNoteRepository {

    void save(@NonNull TradingNote tradingNote);

    TradingNote findById(UUID id);
}
