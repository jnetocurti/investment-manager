package br.com.investmentmanager.tradingnote.domain.service;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;
import lombok.NonNull;

public interface TradingNoteService {

    TradingNote create(@NonNull byte[] bytes);
}
