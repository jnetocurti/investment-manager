package com.investmentmanager.tradingnote.adapter.out.parser;

import com.investmentmanager.commons.domain.model.Broker;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.OperationType;
import com.investmentmanager.tradingnote.domain.model.Fee;
import com.investmentmanager.tradingnote.domain.model.Operation;
import com.investmentmanager.tradingnote.domain.model.PdfProcessingException;
import com.investmentmanager.tradingnote.domain.model.TradingNote;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteParserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PdfTradingNoteParser implements TradingNoteParserPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<NoteExtractor> extractors;

    public static PdfTradingNoteParser createDefault() {
        return new PdfTradingNoteParser(List.of(
                new ClearNoteExtractor(),
                new NuInvestNoteExtractor()
        ));
    }

    @Override
    public TradingNote parse(InputStream pdfContent) {
        String rawText = extractText(pdfContent);
        String normalizedText = TextNormalizer.normalize(rawText);

        NoteExtractor extractor = extractors.stream()
                .filter(e -> e.supports(normalizedText))
                .findFirst()
                .orElseThrow(() -> new PdfProcessingException(
                        "No extractor found for the given trading note PDF"));

        log.debug("Using extractor: {}", extractor.getClass().getSimpleName());
        RawNoteData raw = extractor.extract(normalizedText);

        return toTradingNote(raw);
    }

    private TradingNote toTradingNote(RawNoteData raw) {
        var fees = raw.fees().stream()
                .map(f -> new Fee(f.description(), parseMonetary(f.value())))
                .toList();

        var operations = raw.operations().stream()
                .map(this::toOperation)
                .toList();

        return TradingNote.builder()
                .noteNumber(raw.noteNumber())
                .broker(new Broker(raw.brokerName(), raw.brokerDocumentId()))
                .tradingDate(parseDate(raw.tradingDate()))
                .settlementDate(parseDate(raw.settlementDate()))
                .operations(operations)
                .fees(fees)
                .totalNote(parseMonetary(raw.totalNote()))
                .currency(raw.currency())
                .build();
    }

    private Operation toOperation(RawNoteData.RawOperation raw) {
        var type = "BUY".equals(raw.operationType()) ? OperationType.BUY : OperationType.SELL;
        var unitPrice = parseMonetary(raw.unitPrice());
        var totalValue = parseMonetary(raw.totalValue());
        if (totalValue.isZero()) {
            totalValue = unitPrice.multiply(raw.quantity());
        }
        return Operation.builder()
                .assetDescription(raw.assetDescription())
                .type(type)
                .quantity(raw.quantity())
                .unitPrice(unitPrice)
                .totalValue(totalValue)
                .build();
    }

    private String extractText(InputStream pdfContent) {
        try {
            byte[] bytes = pdfContent.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                if (document.isEncrypted()) {
                    throw new PdfProcessingException("PDF is password-protected and cannot be read");
                }
                return new PDFTextStripper().getText(document);
            }
        } catch (PdfProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to extract text from PDF", e);
        }
    }

    private static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date.trim(), DATE_FORMAT);
    }

    private static MonetaryValue parseMonetary(String value) {
        if (value == null || value.isBlank()) return MonetaryValue.zero();
        return MonetaryValue.of(value.replace(".", "").replace(",", "."));
    }
}
