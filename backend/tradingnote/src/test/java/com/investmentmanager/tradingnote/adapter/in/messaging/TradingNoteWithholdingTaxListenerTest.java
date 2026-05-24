package com.investmentmanager.tradingnote.adapter.in.messaging;

import com.investmentmanager.tradingnote.adapter.out.persistence.TradingNoteWithholdingTaxDocument;
import com.investmentmanager.tradingnote.adapter.out.persistence.TradingNoteWithholdingTaxMongoRepository;
import com.investmentmanager.tradingnote.domain.model.TradingNoteCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TradingNoteWithholdingTaxListenerTest {
    @Test
    void shouldPersistIrrfFromSellNote() {
        TradingNoteWithholdingTaxMongoRepository repository = mock(TradingNoteWithholdingTaxMongoRepository.class);
        when(repository.findByTradingNoteIdAndTaxType("n1", "IRRF")).thenReturn(Optional.empty());
        new TradingNoteWithholdingTaxListener(repository).onTradingNoteCreated(event("n1", BigDecimal.valueOf(1.23), "SELL"));
        verify(repository).save(any(TradingNoteWithholdingTaxDocument.class));
    }

    @Test
    void shouldNotPersistWhenNoIrrf() {
        TradingNoteWithholdingTaxMongoRepository repository = mock(TradingNoteWithholdingTaxMongoRepository.class);
        new TradingNoteWithholdingTaxListener(repository).onTradingNoteCreated(event("n2", BigDecimal.ZERO, "BUY"));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFlagUnexpectedForBuyOnly() {
        TradingNoteWithholdingTaxMongoRepository repository = mock(TradingNoteWithholdingTaxMongoRepository.class);
        when(repository.findByTradingNoteIdAndTaxType("n3", "IRRF")).thenReturn(Optional.empty());
        TradingNoteWithholdingTaxListener listener = new TradingNoteWithholdingTaxListener(repository);
        listener.onTradingNoteCreated(event("n3", BigDecimal.ONE, "BUY"));
        var captor = org.mockito.ArgumentCaptor.forClass(TradingNoteWithholdingTaxDocument.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().isUnexpectedForBuyOnly());
    }

    private static TradingNoteCreatedEvent event(String noteId, BigDecimal irrf, String opType) {
        return TradingNoteCreatedEvent.builder().tradingNoteId(noteId).noteNumber("100")
                .brokerName("CLEAR").brokerDocument("1")
                .tradingDate(LocalDate.of(2026, 1, 10)).settlementDate(LocalDate.of(2026, 1, 12))
                .totalNote(BigDecimal.TEN).totalFees(BigDecimal.ONE).withholdingTaxesTotal(irrf).currency("BRL")
                .operations(List.of(new TradingNoteCreatedEvent.OperationEvent("PETR4", opType, 1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO)))
                .build();
    }
}
