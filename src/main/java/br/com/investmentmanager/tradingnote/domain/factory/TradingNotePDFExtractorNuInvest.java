package br.com.investmentmanager.tradingnote.domain.factory;

import br.com.investmentmanager.shared.util.constants.Currency;
import lombok.Getter;

@Getter
class TradingNotePDFExtractorNuInvest extends TradingNotePDFExtractor {
    private final String broker = "NuInvest";
    private final Currency currency = Currency.BRL;
    private final String brokerPattern = "(62.169.875/0001-79)";
    private final String invoiceNumberPattern = "[Número\\sdant]{15}(\\d+)";
    private final String tradingDatePattern = "[Dat\\sPregão]{10}([\\d/]{10})";
    private final String liquidateDatePattern = "[Líquidopar\\s]{14}([\\d/]{10})";
    private final String totalAmountPattern = "[Líquidopar\\d\\/\\s]{26}[\\s\\-]+([\\d.,]+)";

    private final TradingNoteItemsExtractor tradingNoteItemsExtractor = new TradingNoteItemsNuInvestExtractor();

    TradingNotePDFExtractorNuInvest(TradingNotePDFExtractor next) {
        super(next);
    }

    @Getter
    static class TradingNoteItemsNuInvestExtractor extends TradingNoteItemsExtractor {
        private final Currency currency = Currency.BRL;
        // TODO rever regex para notas diferentes
        private final String itemsPattern = "BOVESPA\\s([CV])[VISTA\\s]+(\\w+)[CIER#\\s]+(\\d+)\\s([\\d,]+)\\s[\\d,]+\\s";

        @Override
        protected int getAssetCodeGroup() {
            return 2;
        }

        @Override
        protected int getQuantityGroup() {
            return 3;
        }

        @Override
        protected int getUnitPriceGroup() {
            return 4;
        }

        @Override
        protected int getOperationGroup() {
            return 1;
        }
    }
}

