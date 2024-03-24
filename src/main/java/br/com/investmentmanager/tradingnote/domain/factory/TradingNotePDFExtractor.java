package br.com.investmentmanager.tradingnote.domain.factory;

import br.com.investmentmanager.shared.util.constants.Currency;
import br.com.investmentmanager.shared.util.constants.Operation;
import br.com.investmentmanager.tradingnote.domain.exceptions.InvalidTradingNoteFileException;
import br.com.investmentmanager.tradingnote.domain.exceptions.UnsupportedTradingNoteContentException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static br.com.investmentmanager.shared.util.DateUtils.parseLocalDate;
import static br.com.investmentmanager.shared.util.NumberUtils.parseBigDecimal;

@Getter
abstract class TradingNotePDFExtractor {

    @Getter(AccessLevel.NONE)
    private final Optional<TradingNotePDFExtractor> next;

    @Getter(AccessLevel.NONE)
    private String content;

    private String invoiceNumber;
    private LocalDate tradingDate;
    private LocalDate liquidateDate;
    private BigDecimal totalAmount;

    public TradingNotePDFExtractor(TradingNotePDFExtractor next) {
        this.next = Optional.ofNullable(next);
    }

    public final TradingNotePDFExtractor extract(@NonNull byte[] bytes) {
        try (PDDocument document = PDDocument.load(bytes)) {
            return extract(new PDFTextStripper().getText(document));
        } catch (IOException ex) {
            throw new InvalidTradingNoteFileException(ex);
        }
    }

    private TradingNotePDFExtractor extract(@NonNull String content) {
        this.content = content;

        if (accept()) {
            invoiceNumber = getMatchedValue(getInvoiceNumberPattern());
            tradingDate = parseLocalDate(getMatchedValue(getTradingDatePattern()));
            liquidateDate = parseLocalDate(getMatchedValue(getLiquidateDatePattern()));
            totalAmount = parseBigDecimal(getMatchedValue(getTotalAmountPattern()));
            getTradingNoteItemsExtractor().extract(content);
            return this;
        } else {
            System.out.println(getMatchedValue(getInvoiceNumberPattern()));
            return next.map(e -> e.extract(this.content)).orElseThrow(UnsupportedTradingNoteContentException::new);
        }
    }

    protected final Matcher getMatcher(String pattern) {
        return Pattern.compile(pattern).matcher(content);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String getMatchedValue(String pattern) {
        var matcher = getMatcher(pattern);
        matcher.find();
        return matcher.group(1);
    }

    private boolean accept() {
        return Stream.of(
                getMatcher(getBrokerPattern()),
                getMatcher(getInvoiceNumberPattern()),
                getMatcher(getTradingDatePattern()),
                getMatcher(getLiquidateDatePattern()),
                getMatcher(getTotalAmountPattern()),
                getMatcher(getTradingNoteItemsExtractor().getItemsPattern())
        ).allMatch(Matcher::find);
    }

    protected abstract String getBroker();

    protected abstract Currency getCurrency();

    protected abstract String getBrokerPattern();

    protected abstract String getInvoiceNumberPattern();

    protected abstract String getTradingDatePattern();

    protected abstract String getLiquidateDatePattern();

    protected abstract String getTotalAmountPattern();

    protected abstract TradingNoteItemsExtractor getTradingNoteItemsExtractor();

    @Getter
    static abstract class TradingNoteItemsExtractor {

        protected List<TradingNoteItemDTO> items;

        public void extract(String content) {
            items = Pattern.compile(getItemsPattern()).matcher(content).results()
                    .map(m -> TradingNoteItemDTO.builder()
                            .currency(getCurrency())
                            .assetCode(getAssetCode(m))
                            .quantity(parseBigDecimal(m.group(getQuantityGroup())))
                            .unitPrice(parseBigDecimal(m.group(getUnitPriceGroup())))
                            .operation(Operation.of(m.group(getOperationGroup())))
                            .build())
                    .toList();
        }

        private String getAssetCode(MatchResult m) {
            var raw = m.group(getAssetCodeGroup()).replaceAll("\s+", " ");

            Matcher matcher = Pattern.compile("(\\w{5,6})").matcher(raw);
            raw = matcher.find() ? matcher.group(1) : raw;

            return raw;
        }

        protected abstract Currency getCurrency();

        protected abstract String getItemsPattern();

        protected abstract int getAssetCodeGroup();

        protected abstract int getQuantityGroup();

        protected abstract int getUnitPriceGroup();

        protected abstract int getOperationGroup();
    }
}
