package br.com.investmentmanager.tradingnote.domain.aggregate;

import br.com.investmentmanager.shared.domain.aggregate.AbstractAggregateRoot;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import br.com.investmentmanager.tradingnote.domain.helper.ClearExtractor;
import br.com.investmentmanager.tradingnote.domain.helper.NuInvestExtractor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.javatuples.Sextet;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;

@Getter
@ToString
public class TradingNote extends AbstractAggregateRoot<TradingNote> {
    private String id;
    private String broker;
    private BigDecimal netAmount;
    private BigDecimal totalAmount;
    private BigDecimal costs;
    private LocalDate tradingDate;
    private LocalDate liquidateDate;
    private Collection<TradingNoteItem> items;
    private TradingNoteFile file;

    public static TradingNote valueOf(@NonNull byte[] bytes) {
        var extractor = new NuInvestExtractor(new ClearExtractor(null)).extract(bytes);

        TradingNote tradingNote = new TradingNote();
        tradingNote.id = extractor.getId();
        tradingNote.broker = extractor.getBroker();
        tradingNote.totalAmount = extractor.getTotalAmount();
        tradingNote.tradingDate = extractor.getTradingDate();
        tradingNote.liquidateDate = extractor.getLiquidateDate();
        tradingNote.setNetAmountAndCosts(extractor.getItems());
        tradingNote.createItems(extractor.getItems());
        tradingNote.createFile(bytes);

        tradingNote.registerEvent(new TradingNoteCreated(tradingNote));
        return tradingNote;
    }

    private void setNetAmountAndCosts(Collection<Sextet<String, BigDecimal, BigDecimal, String, BigDecimal, String>> items) {
        netAmount = items.stream().map(Sextet::getValue4).reduce(BigDecimal.ZERO, BigDecimal::add);
        costs = totalAmount.subtract(netAmount);
    }

    private void createItems(Collection<Sextet<String, BigDecimal, BigDecimal, String, BigDecimal, String>> items) {
        var multiplyer = costs.divide(netAmount, 6, RoundingMode.UP);
        this.items = items.stream().map(i -> {
            var assetCode = i.getValue0();
            var unitPrice = i.getValue1();
            var quantity = i.getValue2();
            var operation = i.getValue3();
            var netAmount = i.getValue4();
            var currency = i.getValue5();
            var totalAmount = netAmount.add(netAmount.multiply(multiplyer)).setScale(6, RoundingMode.UP);
            return TradingNoteItem.of(assetCode, unitPrice, quantity, operation, netAmount, totalAmount, currency);
        }).toList();
    }

    private void createFile(byte[] bytes) {
        var fileName = String.format("%s_%s_%s.pdf", broker, id, tradingDate);
        file = TradingNoteFile.of(fileName, "application/pdf", new ByteArrayInputStream(bytes));

        registerEvent(new TradingNoteFileCreated(file));
    }
}
