package br.com.investmentmanager.tradingnote.domain.helper;

import lombok.Getter;
import org.javatuples.Sextet;

import java.math.BigDecimal;
import java.util.List;

import static br.com.investmentmanager.shared.util.NumberUtils.parseBigDecimal;

@Getter
public class NuInvestExtractor extends PDFExtractor {
    private static final String CURRENCY = "BRL";

    private final String broker = "NuInvest";
    private final String idPattern = "[Número\\sdant]{15}(\\d+)";
    private final String brokerPattern = "(62.169.875/0001-79)";
    private final String tradingDatePattern = "[Dat\\sPregão]{10}([\\d/]{10})";
    private final String liquidateDatePattern = "[Líquidopar\\s]{14}([\\d/]{10})";
    private final String totalAmountPattern = "[Líquidopar\\d\\/\\s]{26}[\\s\\-]+([\\d.,]+)";
    private final String itemsPattern = "BOVESPA\\s([CV])[VISTA\\s]+(\\w+)[CIER\\s]+(\\d+)\\s([\\d,]+)\\s([\\d,]+)\\s";

    public NuInvestExtractor(PDFExtractor next) {
        super(next);
    }

    public List<Sextet<String, BigDecimal, BigDecimal, String, BigDecimal, String>> extractItems() {
        return getMatcher(itemsPattern).results().map(m -> Sextet.with(
                m.group(2).replaceAll("\s+", " "),
                parseBigDecimal(m.group(4)),
                parseBigDecimal(m.group(3)),
                m.group(1),
                parseBigDecimal(m.group(5)),
                CURRENCY
        )).toList();
    }
}
