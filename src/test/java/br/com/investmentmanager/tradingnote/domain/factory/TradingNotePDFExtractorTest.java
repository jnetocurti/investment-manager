package br.com.investmentmanager.tradingnote.domain.factory;

import br.com.investmentmanager.shared.util.constants.Currency;
import br.com.investmentmanager.shared.util.constants.Operation;
import br.com.investmentmanager.tradingnote.domain.exceptions.InvalidTradingNoteFileException;
import br.com.investmentmanager.tradingnote.domain.exceptions.UnsupportedTradingNoteContentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static br.com.investmentmanager.FileUtils.loadFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradingNotePDFExtractorTest {

    @Test
    void extractFromNuInvestTradingNote() {
        var extractor = new TradingNotePDFExtractorClear(new TradingNotePDFExtractorNuInvest(null))
                .extract(loadFile("trading-notes/Invoice_103226.pdf"));

        assertEquals("NuInvest", extractor.getBroker());
        assertEquals(Currency.BRL, extractor.getCurrency());
        assertEquals("103226", extractor.getInvoiceNumber());
        assertEquals(LocalDate.of(2022, 6, 3), extractor.getTradingDate());
        assertEquals(LocalDate.of(2022, 6, 7), extractor.getLiquidateDate());
        assertEquals(new BigDecimal("624.91"), extractor.getTotalAmount());

        var tradingNoteItems = extractor.getTradingNoteItemsExtractor().getItems();
        assertEquals(3, tradingNoteItems.size());

        assertEquals(Currency.BRL, tradingNoteItems.get(0).getCurrency());
        assertEquals("CPTS11", tradingNoteItems.get(0).getAssetCode());
        assertEquals(new BigDecimal("2"), tradingNoteItems.get(0).getQuantity());
        assertEquals(new BigDecimal("94.60"), tradingNoteItems.get(0).getUnitPrice());
        assertEquals(Operation.BUY, tradingNoteItems.get(0).getOperation());

        assertEquals(Currency.BRL, tradingNoteItems.get(1).getCurrency());
        assertEquals("KNRI11", tradingNoteItems.get(1).getAssetCode());
        assertEquals(new BigDecimal("3"), tradingNoteItems.get(1).getQuantity());
        assertEquals(new BigDecimal("132.19"), tradingNoteItems.get(1).getUnitPrice());
        assertEquals(Operation.BUY, tradingNoteItems.get(1).getOperation());

        assertEquals(Currency.BRL, tradingNoteItems.get(2).getCurrency());
        assertEquals("MXRF11", tradingNoteItems.get(2).getAssetCode());
        assertEquals(new BigDecimal("4"), tradingNoteItems.get(2).getQuantity());
        assertEquals(new BigDecimal("9.74"), tradingNoteItems.get(2).getUnitPrice());
        assertEquals(Operation.BUY, tradingNoteItems.get(2).getOperation());
    }

    @Test
    void extractFromClearTradingNote() {
        var extractor = new TradingNotePDFExtractorNuInvest(new TradingNotePDFExtractorClear(null))
                .extract(loadFile("trading-notes/Invoice_19889017.pdf"));

        assertEquals("Clear", extractor.getBroker());
        assertEquals(Currency.BRL, extractor.getCurrency());
        assertEquals("19889017", extractor.getInvoiceNumber());
        assertEquals(LocalDate.of(2021, 12, 16), extractor.getTradingDate());
        assertEquals(LocalDate.of(2021, 12, 20), extractor.getLiquidateDate());
        assertEquals(new BigDecimal("58.65"), extractor.getTotalAmount());

        var tradingNoteItems = extractor.getTradingNoteItemsExtractor().getItems();
        assertEquals(2, tradingNoteItems.size());

        assertEquals(Currency.BRL, tradingNoteItems.get(0).getCurrency());
        assertEquals("ENGIE BRASIL ON NM", tradingNoteItems.get(0).getAssetCode());
        assertEquals(new BigDecimal("1"), tradingNoteItems.get(0).getQuantity());
        assertEquals(new BigDecimal("38.93"), tradingNoteItems.get(0).getUnitPrice());
        assertEquals(Operation.BUY, tradingNoteItems.get(0).getOperation());

        assertEquals(Currency.BRL, tradingNoteItems.get(1).getCurrency());
        assertEquals("ITAUUNIBANCO ON N1", tradingNoteItems.get(1).getAssetCode());
        assertEquals(new BigDecimal("1"), tradingNoteItems.get(1).getQuantity());
        assertEquals(new BigDecimal("19.71"), tradingNoteItems.get(1).getUnitPrice());
        assertEquals(Operation.BUY, tradingNoteItems.get(1).getOperation());
    }

    @Test
    void extractFromUnsupportedTradingNote() {
        var unsupported = loadFile("trading-notes/Invoice_unsupported.pdf");
        assertThrows(UnsupportedTradingNoteContentException.class, () ->
                new TradingNotePDFExtractorNuInvest(null).extract(unsupported));
    }

    @Test
    void extractFromInvalidTradingNote() {
        assertThrows(InvalidTradingNoteFileException.class, () ->
                new TradingNotePDFExtractorNuInvest(null).extract(new byte[]{}));
    }
}