package br.com.investmentmanager.tradingnote.domain.helper;

import lombok.Getter;
import org.javatuples.Sextet;

import java.math.BigDecimal;
import java.util.List;

import static br.com.investmentmanager.shared.util.NumberUtils.parseBigDecimal;

@Getter
public class ClearExtractor extends PDFExtractor {
    private static final String CURRENCY = "BRL";

    private final String broker = "Clear";
    private final String idPattern = "[Nr.nota\\s]{10}([\\d]+)";
    private final String brokerPattern = "(02.332.886/0011-78)";
    private final String tradingDatePattern = "[Datpregão\\s]{13}([\\d/]{10})";
    private final String liquidateDatePattern = "[Líquidopar\\s]{12}([\\d/]{10})";
    private final String totalAmountPattern = "[OutrosC\\s]{8}([\\d.,]+)[Líquidopar\\s]{12}";
    private final String itemsPattern = "([CV]{1})[FRACION\\s]{13}(.*)\\s(\\d)\\s([\\d.,]+)\\s([\\d.,]+)";

    public ClearExtractor(PDFExtractor next) {
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
