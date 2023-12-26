package br.com.investmentmanager.tradingnote.integration.storage;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNoteFile;
import br.com.investmentmanager.tradingnote.domain.repository.TradingNoteFileRepository;
import org.springframework.stereotype.Component;

@Component
public class TradingNoteFileRepositoryImpl implements TradingNoteFileRepository {

    @Override
    public void save(TradingNoteFile file) {
    }
}
