package br.com.investmentmanager.tradingnote.domain.factory;

import br.com.investmentmanager.shared.util.constants.Currency;
import lombok.Getter;

@Getter
class TradingNotePDFExtractorClear extends TradingNotePDFExtractor {
    private final String broker = "Clear";
    private final Currency currency = Currency.BRL;
    private final String brokerPattern = "(02.332.886/0011-78)";
    private final String invoiceNumberPattern = "[Nr.nota\\s]{10}([\\d]+)";
    private final String tradingDatePattern = "[Datpregão\\s]{13}([\\d/]{10})";
    private final String liquidateDatePattern = "[Líquidopar\\s]{12}([\\d/]{10})";
    private final String totalAmountPattern = "[OutrosC\\s]{8}([\\d.,]+)[Líquidopar\\s]{12}";

    private final TradingNoteItemsExtractor tradingNoteItemsExtractor = new TradingNoteItemsNuInvestExtractor();

    TradingNotePDFExtractorClear(TradingNotePDFExtractor next) {
        super(next);
    }

    @Getter
    static class TradingNoteItemsNuInvestExtractor extends TradingNoteItemsExtractor {
        private final Currency currency = Currency.BRL;
        private final String itemsPattern = "([CV]{1})[FRACION\\s]{13}(.*)\\s(\\d)\\s([\\d.,]+)\\s([\\d.,]+)";

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

