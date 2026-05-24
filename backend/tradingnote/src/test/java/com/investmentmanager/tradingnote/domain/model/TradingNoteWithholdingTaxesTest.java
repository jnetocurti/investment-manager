package com.investmentmanager.tradingnote.domain.model;

import com.investmentmanager.commons.domain.model.Broker;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.OperationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradingNoteWithholdingTaxesTest {

    @Test
    void buyOnlyWithoutIrrfShouldKeepOperationalFees() {
        TradingNote note = TradingNote.builder().noteNumber("1").broker(new Broker("CLEAR", "1"))
                .tradingDate(LocalDate.of(2026, 1, 10)).settlementDate(LocalDate.of(2026, 1, 12))
                .operations(List.of(op(OperationType.BUY, "100", 10), op(OperationType.BUY, "100", 5)))
                .fees(List.of(new Fee("Corretagem", MonetaryValue.of("10.00"))))
                .totalNote(MonetaryValue.of("210.00")).build();

        assertEquals(MonetaryValue.of("10.00").toBigDecimal(), note.getTotalFees().toBigDecimal());
        assertEquals(MonetaryValue.zero().toBigDecimal(), note.getWithholdingTaxesTotal().toBigDecimal());
    }

    @Test
    void sellOnlyWithIrrfShouldExcludeFromOperationalFees() {
        TradingNote note = TradingNote.builder().noteNumber("2").broker(new Broker("CLEAR", "1"))
                .tradingDate(LocalDate.of(2026, 1, 10)).settlementDate(LocalDate.of(2026, 1, 12))
                .operations(List.of(op(OperationType.SELL, "100", 10)))
                .fees(List.of(new Fee("Taxa de liquidação", MonetaryValue.of("2.00")), new Fee("I.R.R.F.", MonetaryValue.of("1.00"))))
                .totalNote(MonetaryValue.of("97.00")).build();

        assertEquals(MonetaryValue.of("2.00").toBigDecimal(), note.getTotalFees().toBigDecimal());
        assertEquals(MonetaryValue.of("1.00").toBigDecimal(), note.getWithholdingTaxesTotal().toBigDecimal());
    }

    @Test
    void mixedWithIrrfShouldExcludeFromOperationalFees() {
        TradingNote note = TradingNote.builder().noteNumber("3").broker(new Broker("CLEAR", "1"))
                .tradingDate(LocalDate.of(2026, 1, 10)).settlementDate(LocalDate.of(2026, 1, 12))
                .operations(List.of(op(OperationType.BUY, "100", 5), op(OperationType.SELL, "200", 5)))
                .fees(List.of(new Fee("Corretagem", MonetaryValue.of("10.00")), new Fee("IRRF", MonetaryValue.of("2.00"))))
                .totalNote(MonetaryValue.of("88.00")).build();

        assertEquals(MonetaryValue.of("10.00").toBigDecimal(), note.getTotalFees().toBigDecimal());
        assertEquals(MonetaryValue.of("2.00").toBigDecimal(), note.getWithholdingTaxesTotal().toBigDecimal());
    }

    private static Operation op(OperationType type, String totalValue, int qty) {
        return Operation.builder().assetDescription("PETR4").type(type).quantity(qty)
                .unitPrice(MonetaryValue.of("10.00")).totalValue(MonetaryValue.of(totalValue)).build();
    }
}
