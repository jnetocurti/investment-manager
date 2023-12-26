package br.com.investmentmanager.tradingnote.infrastructure.database;

import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.TradingNoteItem;
import br.com.investmentmanager.tradingnote.domain.TradingNoteRepository;
import br.com.investmentmanager.tradingnote.domain.exceptions.TradingNoteAlreadyExistsException;
import br.com.investmentmanager.tradingnote.domain.exceptions.TradingNoteNotFoundException;
import br.com.investmentmanager.tradingnote.infrastructure.database.model.PersistenceTradingNote;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import java.util.UUID;

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

    @Override
    public TradingNote findById(UUID id) {
        var persistenceTradingNote = tradingNoteDAO.findById(id).orElseThrow(TradingNoteNotFoundException::new);

        return TradingNote.builder()
                .id(persistenceTradingNote.getId())
                .broker(persistenceTradingNote.getBroker())
                .invoiceNumber(persistenceTradingNote.getInvoiceNumber())
                .netAmount(persistenceTradingNote.getNetAmount())
                .costs(persistenceTradingNote.getCosts())
                .tradingDate(persistenceTradingNote.getTradingDate())
                .liquidateDate(persistenceTradingNote.getLiquidateDate())
                .items(persistenceTradingNote.getItems()
                        .stream()
                        .map(i -> TradingNoteItem.builder()
                                .id(i.getId())
                                .assetCode(i.getAssetCode())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .costs(i.getCosts())
                                .operation(i.getOperation())
                                .build())
                        .toList())
                .build();
    }
}
