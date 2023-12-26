package br.com.investmentmanager.tradingnote.domain.helper;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;
import br.com.investmentmanager.tradingnote.domain.exceptions.InvalidTradingNoteFile;
import br.com.investmentmanager.tradingnote.domain.exceptions.UnsupportedTradingNoteContent;
import org.javatuples.Sextet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static br.com.investmentmanager.FileUtils.loadFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PDFExtractorTest {

    @Test
    void extractFromNuInvestTradingNote() {
        var extractor = new ClearExtractor(new NuInvestExtractor(null))
                .extract(loadFile("trading-notes/Invoice_103226.pdf"));

        assertEquals("103226", extractor.getId());
        assertEquals("NuInvest", extractor.getBroker());
        assertEquals(new BigDecimal("624.91"), extractor.getTotalAmount());
        assertEquals(LocalDate.of(2022, 6, 3), extractor.getTradingDate());
        assertEquals(LocalDate.of(2022, 6, 7), extractor.getLiquidateDate());

        var items = extractor.getItems().stream().toList();
        assertEquals(3, items.size());
        assertEquals(Sextet.with("CPTS11", new BigDecimal("94.60"), new BigDecimal("2"), "C", new BigDecimal("189.20"),
                "BRL"), items.get(0));
        assertEquals(Sextet.with("KNRI11", new BigDecimal("132.19"), new BigDecimal("3"), "C", new BigDecimal("396.57"),
                "BRL"), items.get(1));
        assertEquals(Sextet.with("MXRF11", new BigDecimal("9.74"), new BigDecimal("4"), "C", new BigDecimal("38.96"),
                "BRL"), items.get(2));
    }

    @Test
    void extractFromClearTradingNote() {
        var extractor = new NuInvestExtractor(new ClearExtractor(null))
                .extract(loadFile("trading-notes/Invoice_19889017.pdf"));

        assertEquals("19889017", extractor.getId());
        assertEquals("Clear", extractor.getBroker());
        assertEquals(new BigDecimal("58.65"), extractor.getTotalAmount());
        assertEquals(LocalDate.of(2021, 12, 16), extractor.getTradingDate());
        assertEquals(LocalDate.of(2021, 12, 20), extractor.getLiquidateDate());

        var items = extractor.getItems().stream().toList();
        assertEquals(2, items.size());
        assertEquals(Sextet.with("ENGIE BRASIL ON NM", new BigDecimal("38.93"), new BigDecimal("1"), "C",
                new BigDecimal("38.93"), "BRL"), items.get(0));
        assertEquals(Sextet.with("ITAUUNIBANCO ON N1", new BigDecimal("19.71"), new BigDecimal("1"), "C",
                new BigDecimal("19.71"), "BRL"), items.get(1));
    }

    @Test
    void extractFromUnsupportedTradingNote() {
        var unsupported = loadFile("trading-notes/Invoice_unsupported.pdf");
        assertThrows(UnsupportedTradingNoteContent.class, () -> TradingNote.valueOf(unsupported));
    }

    @Test
    void extractFromInvalidTradingNote() {
        assertThrows(InvalidTradingNoteFile.class, () -> TradingNote.valueOf(new byte[]{}));
    }
}