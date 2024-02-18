package br.com.investmentmanager.portfolioevent.domain.factory;

import br.com.investmentmanager.portfolioevent.application.input.TradingNoteMessage;
import br.com.investmentmanager.portfolioevent.domain.PortfolioEvent;
import br.com.investmentmanager.portfolioevent.domain.valueobjects.Asset;
import org.springframework.stereotype.Component;

import java.util.List;

import static br.com.investmentmanager.shared.util.constants.AssetType.BRAZILIAN_STOCKS;
import static br.com.investmentmanager.shared.util.constants.AssetType.REAL_ESTATE_FUND;

@Component
public class PortfolioEventFactory {

    public List<PortfolioEvent> createFromTradingNote(TradingNoteMessage tradingNote) {
        return tradingNote.getItems()
                .stream()
                .map(i -> PortfolioEvent.builder()
                        .broker(tradingNote.getBroker())
                        .invoiceNumber(tradingNote.getInvoiceNumber())
                        .eventDate(tradingNote.getTradingDate())
                        .eventLiquidateDate(tradingNote.getLiquidateDate())
                        .operation(i.getOperation())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalAmount(i.getTotalAmount())
                        .netAmount(i.getNetAmount())
                        .costs(i.getCosts())
                        .asset(createAsset(i.getAssetCode()))
                        .build())
                .toList();
    }

    // TODO dependencia modulo Asset para classificação e criação de ativos
    private static Asset createAsset(String assetCode) {
        assetCode = switch (assetCode) {
            case "ENGIE BRASIL ON NM" -> "EGIE3";
            case "ITAUUNIBANCO ON N1" -> "ITUB3";
            default -> assetCode;
        };

        var assetType = switch (assetCode) {
            case "EGIE3", "ITUB3" -> BRAZILIAN_STOCKS;
            default -> REAL_ESTATE_FUND;
        };

        return Asset.builder().assetCode(assetCode).assetType(assetType).build();
    }
}
