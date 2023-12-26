package br.com.investmentmanager.tradingnote.domain.aggregate;

import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import br.com.investmentmanager.tradingnote.domain.exceptions.InvalidTradingNoteFile;
import br.com.investmentmanager.tradingnote.domain.exceptions.UnsupportedTradingNoteContent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TradingNoteTest {

    private static TradingNote tradingNote;

    @BeforeAll
    public static void setup() {
        tradingNote = TradingNote.valueOf(getTradingNoteBytes("trading-notes/Invoice_103226.pdf"));
    }

    @SneakyThrows
    private static byte[] getTradingNoteBytes(String file) {
        try (var inputStream = TradingNoteTest.class.getClassLoader().getResourceAsStream(file)) {
            assert inputStream != null;
            return inputStream.readAllBytes();
        }
    }

    @Test
    void valueOfShouldCreateBasicData() {
        assertEquals("NuInvest", tradingNote.getBroker());
        assertEquals("103226", tradingNote.getId());
        assertEquals(new BigDecimal("0.18"), tradingNote.getCosts());
        assertEquals(new BigDecimal("624.73"), tradingNote.getNetAmount());
        assertEquals(new BigDecimal("624.91"), tradingNote.getTotalAmount());
        assertEquals(LocalDate.of(2022, 6, 3), tradingNote.getTradingDate());
        assertEquals(LocalDate.of(2022, 6, 7), tradingNote.getLiquidateDate());
    }

    @Test
    void valueOfShouldCreateItemsData() {
        var items = tradingNote.getItems().stream().toList();

        assertEquals(3, items.size());
        assertEquals(TradingNoteItem.of("CPTS11", new BigDecimal("94.60"), new BigDecimal("2"), "C",
                new BigDecimal("189.20"), new BigDecimal("189.254679"), "BRL"), items.get(0));
        assertEquals(TradingNoteItem.of("KNRI11", new BigDecimal("132.19"),
                new BigDecimal("3"), "C", new BigDecimal("396.57"), new BigDecimal("396.684609"), "BRL"), items.get(1));
        assertEquals(TradingNoteItem.of("MXRF11", new BigDecimal("9.74"),
                new BigDecimal("4"), "C", new BigDecimal("38.96"), new BigDecimal("38.971260"), "BRL"), items.get(2));

        var itemsTotalAmount = items.stream().map(TradingNoteItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        assertEquals(tradingNote.getTotalAmount(), itemsTotalAmount);
    }

    @Test
    void valueOfShouldCreateFileData() {
        assertNotNull(tradingNote.getFile().getContent());
        assertEquals("application/pdf", tradingNote.getFile().getContentType());
        assertEquals("NuInvest_103226_2022-06-03.pdf", tradingNote.getFile().getName());
    }

    @Test
    void valueOfShouldCreateDomainEvents() {
        var domainEvents = tradingNote.getDomainEvents().stream().toList();

        assertEquals(2, domainEvents.size());
        assertEquals(tradingNote.getFile(), ((TradingNoteFileCreated) domainEvents.get(0)).getSource());
        assertEquals(tradingNote, ((TradingNoteCreated) domainEvents.get(1)).getSource());
    }

    @Test
    void valueOfFromUnsupportedFile() {
        var unsupported = getTradingNoteBytes("trading-notes/Invoice_unsupported.pdf");
        assertThrows(UnsupportedTradingNoteContent.class, () -> TradingNote.valueOf(unsupported));
    }

    @Test
    void valueOfFromInvalidFile() {
        assertThrows(InvalidTradingNoteFile.class, () -> TradingNote.valueOf(new byte[]{}));
    }
}