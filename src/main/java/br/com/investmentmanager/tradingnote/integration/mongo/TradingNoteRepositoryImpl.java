package br.com.investmentmanager.tradingnote.integration.mongo;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;
import br.com.investmentmanager.tradingnote.domain.repository.TradingNoteRepository;
import org.springframework.stereotype.Component;

@Component
public class TradingNoteRepositoryImpl implements TradingNoteRepository {

    @Override
    public void save(TradingNote tradingNote) {
    }
}
