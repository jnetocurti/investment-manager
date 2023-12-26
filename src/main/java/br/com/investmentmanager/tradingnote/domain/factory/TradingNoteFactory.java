package br.com.investmentmanager.tradingnote.domain.factory;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.TradingNoteItem;
import br.com.investmentmanager.tradingnote.domain.valueobjects.TradingNoteFile;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@UtilityClass
public final class TradingNoteFactory {

    public static TradingNote createWithTradingNoteFile(@NonNull byte[] bytes) {
        var extractor = new TradingNotePDFExtractorNuInvest(new TradingNotePDFExtractorClear(null)).extract(bytes);

        var currency = extractor.getCurrency();
        var items = extractor.getTradingNoteItemsExtractor().getItems();

        var netAmount = items.stream()
                .map(i -> MonetaryValue.of(i.getCurrency(), i.getUnitPrice().multiply(i.getQuantity())))
                .reduce(MonetaryValue.of(currency, BigDecimal.ZERO), MonetaryValue::add);

        var totalAmount = MonetaryValue.of(currency, extractor.getTotalAmount());
        var costs = totalAmount.subtract(netAmount);

        var costMultiplier = costs.getValue().divide(netAmount.getValue(), 6, RoundingMode.HALF_UP);

        return TradingNote.builder()
                .broker(extractor.getBroker())
                .invoiceNumber(extractor.getInvoiceNumber())
                .tradingDate(extractor.getTradingDate())
                .liquidateDate(extractor.getLiquidateDate())
                .netAmount(netAmount)
                .costs(costs)
                .items(createItems(items, costMultiplier))
                .file(createTradingNoteFile(extractor, bytes))
                .build();
    }

    private static List<TradingNoteItem> createItems(List<TradingNoteItemDTO> items, BigDecimal costMultiplier) {
        return items.stream().map(i -> {
            var quantity = i.getQuantity();
            var unitPrice = MonetaryValue.of(i.getCurrency(), i.getUnitPrice());

            var netAmount = unitPrice.multiply(quantity);
            var costs = netAmount.add(netAmount.multiply(costMultiplier)).subtract(netAmount);

            return TradingNoteItem.builder()
                    .assetCode(i.getAssetCode())
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .costs(costs)
                    .operation(i.getOperation())
                    .build();
        }).toList();
    }

    private static TradingNoteFile createTradingNoteFile(TradingNotePDFExtractor extractor, byte[] bytes) {
        var fileName = String.format("%s_%s_%s.pdf",
                extractor.getBroker(), extractor.getInvoiceNumber(), extractor.getTradingDate());

        return TradingNoteFile.of(fileName, "application/pdf", new ByteArrayInputStream(bytes));
    }
}
