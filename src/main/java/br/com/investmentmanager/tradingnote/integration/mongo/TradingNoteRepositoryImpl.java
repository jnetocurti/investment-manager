package br.com.investmentmanager.tradingnote.integration.mongo;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;
import br.com.investmentmanager.tradingnote.domain.repository.TradingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradingNoteRepositoryImpl implements TradingNoteRepository {

    private final MongoRepository<TradingNote, String> mongoRepository;

    @Override
    public void save(TradingNote tradingNote) {
        mongoRepository.save(tradingNote);
    }
}