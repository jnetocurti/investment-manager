package br.com.investmentmanager.tradingnote.domain.helper;

import br.com.investmentmanager.tradingnote.domain.exceptions.InvalidTradingNoteFile;
import br.com.investmentmanager.tradingnote.domain.exceptions.UnsupportedTradingNoteContent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.javatuples.Sextet;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static br.com.investmentmanager.shared.util.DateUtils.parseLocalDate;
import static br.com.investmentmanager.shared.util.NumberUtils.parseBigDecimal;

@Getter
public abstract class PDFExtractor implements TradingNoteExtractor {

    @Getter(AccessLevel.NONE)
    private final Optional<PDFExtractor> next;

    @Getter(AccessLevel.NONE)
    private String content;

    private String id;
    private BigDecimal totalAmount;
    private LocalDate tradingDate;
    private LocalDate liquidateDate;
    private Collection<Sextet<String, BigDecimal, BigDecimal, String, BigDecimal, String>> items;

    public PDFExtractor(PDFExtractor next) {
        this.next = Optional.ofNullable(next);
    }

    @Override
    public final TradingNoteExtractor extract(@NonNull byte[] bytes) {
        try (PDDocument document = PDDocument.load(bytes)) {
            return extract(new PDFTextStripper().getText(document));
        } catch (IOException ex) {
            throw new InvalidTradingNoteFile(ex);
        }
    }

    @Override
    public final TradingNoteExtractor extract(@NonNull String content) {
        this.content = content;
        return extract();
    }

    private PDFExtractor extract() {
        if (accept()) {
            id = getMatchedValue(getIdPattern());
            totalAmount = parseBigDecimal(getMatchedValue(getTotalAmountPattern()));
            tradingDate = parseLocalDate(getMatchedValue(getTradingDatePattern()));
            liquidateDate = parseLocalDate(getMatchedValue(getLiquidateDatePattern()));
            items = extractItems();
        } else {
            next.map(e -> e.extract(this.content)).orElseThrow(UnsupportedTradingNoteContent::new);
        }
        return this;
    }

    protected final Matcher getMatcher(String pattern) {
        return Pattern.compile(pattern).matcher(content);
    }

    private String getMatchedValue(String pattern) {
        var matcher = getMatcher(pattern);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean accept() {
        return Stream.of(
                getMatcher(getBrokerPattern()),
                getMatcher(getIdPattern()),
                getMatcher(getTotalAmountPattern()),
                getMatcher(getTradingDatePattern()),
                getMatcher(getLiquidateDatePattern()),
                getMatcher(getItemsPattern())
        ).map(Matcher::find).noneMatch(b -> b.equals(Boolean.FALSE));
    }

    protected abstract String getBrokerPattern();

    protected abstract String getIdPattern();

    protected abstract String getTotalAmountPattern();

    protected abstract String getTradingDatePattern();

    protected abstract String getLiquidateDatePattern();

    protected abstract String getItemsPattern();

    protected abstract Collection<Sextet<String, BigDecimal, BigDecimal, String, BigDecimal, String>> extractItems();
}
