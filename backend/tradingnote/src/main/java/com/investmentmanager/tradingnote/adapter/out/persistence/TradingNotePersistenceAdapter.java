package com.investmentmanager.tradingnote.adapter.out.persistence;

import com.investmentmanager.tradingnote.domain.model.TradingNote;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TradingNotePersistenceAdapter implements TradingNoteRepositoryPort {

    private final TradingNoteMongoRepository mongoRepository;

    @Override
    public TradingNote save(TradingNote tradingNote) {
        TradingNoteDocument doc = TradingNoteDocumentMapper.toDocument(tradingNote);
        TradingNoteDocument saved = mongoRepository.save(doc);
        return TradingNoteDocumentMapper.toDomain(saved);
    }

    @Override
    public boolean existsByFileHash(String fileHash) {
        return mongoRepository.existsByFileHash(fileHash);
    }

    @Override
    public Optional<TradingNote> findByFileHash(String fileHash) {
        return mongoRepository.findByFileHash(fileHash)
                .map(TradingNoteDocumentMapper::toDomain);
    }
}
