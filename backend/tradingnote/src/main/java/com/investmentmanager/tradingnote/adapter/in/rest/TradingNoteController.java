package com.investmentmanager.tradingnote.adapter.in.rest;

import com.investmentmanager.tradingnote.domain.model.PdfProcessingException;
import com.investmentmanager.tradingnote.domain.model.TradingNote;
import com.investmentmanager.tradingnote.domain.model.TradingNoteValidationException;
import com.investmentmanager.tradingnote.domain.port.in.ProcessTradingNoteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/trading-notes")
@RequiredArgsConstructor
public class TradingNoteController {

    private final ProcessTradingNoteUseCase processTradingNote;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTradingNote(@RequestParam("file") MultipartFile file) {
        try {
            TradingNote result = processTradingNote.process(
                    file.getOriginalFilename(), file.getInputStream(), file.getSize());
            return ResponseEntity.ok(new TradingNoteResponse(
                    result.getId(), result.getNoteNumber(), result.getBroker().getName(),
                    result.getTradingDate().toString(), result.getOperations().size(),
                    result.getTotalNote().toString(), result.getTotalFees().toString()));
        } catch (TradingNoteValidationException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (PdfProcessingException e) {
            log.warn("PDF processing error: {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        } catch (IOException e) {
            log.error("IO error reading file", e);
            return ResponseEntity.internalServerError().body("Failed to read file");
        }
    }

    record TradingNoteResponse(String id, String noteNumber, String broker,
                                String tradingDate, int operationsCount,
                                String totalNote, String totalFees) {}
}
