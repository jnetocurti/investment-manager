package br.com.investmentmanager.tradingnote.domain.helper;

import lombok.Getter;
import org.javatuples.Sextet;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ClearExtractor extends PDFExtractor {
    private final String currency = "BRL";
    private final String broker = "NuInvest";
    private final String idPattern = "[Número\\sdant]{15}(\\d+)";
    private final String brokerPattern = "(62.169.875/0001-79)";
    private final String tradingDatePattern = "[Dat\\sPregão]{10}([\\d/]{10})";
    private final String liquidateDatePattern = "[Líquidopar\\s]{14}([\\d/]{10})";
    private final String totalAmountPattern = "[Líquidopar\\d\\/\\s]{26}[\\s\\-]+([\\d.,]+)";
    private final String itemsPattern = "BOVESPA\\s(C|V)[VISTA\\s]+([\\w]+)[CIER\\s]+(\\d+)\\s([\\d,]+)\\s([\\d,]+)\\s";

    public ClearExtractor(PDFExtractor next) {
        super(next);
    }

    @Override
    protected List<Sextet<String, BigDecimal, BigDecimal, String, BigDecimal, String>> extractItems() {
        return null;
    }
}
