package br.com.investmentmanager.portfolioevent.domain.factory;

import br.com.investmentmanager.portfolioevent.application.input.TradingNoteMessage;
import br.com.investmentmanager.portfolioevent.domain.PortfolioEvent;
import br.com.investmentmanager.portfolioevent.domain.valueobjects.Asset;
import org.springframework.stereotype.Component;

import java.util.List;

import static br.com.investmentmanager.shared.util.constants.AssetType.*;

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
            case "ITAUUN" -> "ITUB3";
            case "BRADES" -> "BBDC3";
            case "ENGIE" -> "EGIE3";
            case "MGLU3F" -> "MGLU3";
            case "PETR4F" -> "PETR4";
            case "XP INC DR1" -> "XPBR31";
            default -> assetCode;
        };

        var assetType = switch (assetCode) {
            case "EGIE3", "ITUB3", "BBDC3", "PETR4", "MGLU3" -> BRAZILIAN_STOCKS;
            case "HASH11", "NDIV11" -> EXCHANGE_TRADED_FUNDS;
            case "AMZO34", "NFLX34", "XPBR31" -> BRAZILIAN_DEPOSITARY_RECEIPT;
            case "MXRF12", "KNRI12" -> REAL_ESTATE_FUND_DIREITO;
            default -> REAL_ESTATE_FUND;
        };

        return Asset.builder().assetCode(assetCode).assetType(assetType).build();
    }
}
