package br.com.investmentmanager.tradingnote.domain.factory;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Currency;
import br.com.investmentmanager.shared.util.constants.Operation;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.TradingNoteItem;
import br.com.investmentmanager.tradingnote.domain.valueobjects.TradingNoteFile;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static br.com.investmentmanager.FileUtils.loadFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TradingNoteFactoryTest {

    @Test
    @SneakyThrows
    void createWithTradingNoteFile() {
        var file = loadFile("trading-notes/Invoice_103226.pdf");

        var expected = TradingNote.builder()
                .broker("NuInvest")
                .invoiceNumber("103226")
                .netAmount(MonetaryValue.of(Currency.BRL, new BigDecimal("624.73")))
                .costs(MonetaryValue.of(Currency.BRL, new BigDecimal("0.18")))
                .tradingDate(LocalDate.of(2022, 6, 3))
                .liquidateDate(LocalDate.of(2022, 6, 7))
                .items(List.of(
                        TradingNoteItem.builder()
                                .assetCode("CPTS11")
                                .quantity(BigDecimal.valueOf(2))
                                .unitPrice(MonetaryValue.of(Currency.BRL, new BigDecimal("94.60")))
                                .costs(MonetaryValue.of(Currency.BRL, new BigDecimal("0.05448960")))
                                .operation(Operation.BUY)
                                .build(),
                        TradingNoteItem.builder()
                                .assetCode("KNRI11")
                                .quantity(BigDecimal.valueOf(3))
                                .unitPrice(MonetaryValue.of(Currency.BRL, new BigDecimal("132.19")))
                                .costs(MonetaryValue.of(Currency.BRL, new BigDecimal("0.11421216")))
                                .operation(Operation.BUY)
                                .build(),
                        TradingNoteItem.builder()
                                .assetCode("MXRF11")
                                .quantity(BigDecimal.valueOf(4))
                                .unitPrice(MonetaryValue.of(Currency.BRL, new BigDecimal("9.74")))
                                .costs(MonetaryValue.of(Currency.BRL, new BigDecimal("0.01122048")))
                                .operation(Operation.BUY)
                                .build()))
                .file(TradingNoteFile.of("NuInvest_103226_2022-06-03.pdf", "application/pdf", new ByteArrayInputStream(file)))
                .build();

        var tradingNote = TradingNoteFactory.createWithTradingNoteFile(file);

        assertThat(expected.getFile().getContent().readAllBytes())
                .isEqualTo(tradingNote.getFile().getContent().readAllBytes());

        assertThat(expected).usingRecursiveComparison()
                .ignoringFields("id", "items.id", "file.content", "domainEvents").isEqualTo(tradingNote);
    }
}