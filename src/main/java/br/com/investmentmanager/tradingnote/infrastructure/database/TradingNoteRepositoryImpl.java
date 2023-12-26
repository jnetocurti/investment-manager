package br.com.investmentmanager.tradingnote.infrastructure.database;

import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.TradingNoteRepository;
import br.com.investmentmanager.tradingnote.domain.exceptions.TradingNoteAlreadyExistsException;
import br.com.investmentmanager.tradingnote.infrastructure.database.model.PersistenceTradingNote;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TradingNoteRepositoryImpl implements TradingNoteRepository {

    private final PersistenceTradingNoteDAO tradingNoteDAO;

    @Override
    public void save(@NonNull TradingNote tradingNote) {
        try {
            tradingNoteDAO.save(new ModelMapper().map(tradingNote, PersistenceTradingNote.class));
        } catch (DuplicateKeyException e) {

            var example = new PersistenceTradingNote();
            example.setBroker(tradingNote.getBroker());
            example.setInvoiceNumber(tradingNote.getInvoiceNumber());

            @SuppressWarnings("OptionalGetWithoutIsPresent")
            var persistenceTradingNote = tradingNoteDAO.findOne(Example.of(example)).get();

            throw new TradingNoteAlreadyExistsException(persistenceTradingNote.getId());
        }
    }
}
