package com.investmentmanager.tradingnote.domain.service;

import com.investmentmanager.tradingnote.domain.model.TradingNote;
import com.investmentmanager.tradingnote.domain.model.TradingNoteCreatedEvent;
import com.investmentmanager.tradingnote.domain.port.in.ProcessTradingNoteUseCase;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteParserPort;
import com.investmentmanager.tradingnote.domain.port.out.FileStoragePort;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteEventPublisherPort;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RequiredArgsConstructor
public class ProcessTradingNoteService implements ProcessTradingNoteUseCase {

    private final TradingNoteParserPort parser;
    private final TradingNoteRepositoryPort repository;
    private final TradingNoteEventPublisherPort eventPublisher;
    private final FileStoragePort fileStorage;

    @Override
    public TradingNote process(String filename, InputStream pdfContent, long fileSize) {
        byte[] pdfBytes = readBytes(pdfContent);
        String fileHash = computeHash(pdfBytes);

        if (repository.existsByFileHash(fileHash)) {
            log.debug("Trading note already processed (hash={})", fileHash);
            return repository.findByFileHash(fileHash).orElseThrow();
        }

        log.debug("Parsing PDF: {}", filename);
        TradingNote tradingNote = parser.parse(new ByteArrayInputStream(pdfBytes))
                .withFileHash(fileHash);

        log.debug("Storing PDF as: {}", tradingNote.buildStorageFilename());
        String fileReference = fileStorage.store(
                tradingNote.buildStorageFilename(),
                new ByteArrayInputStream(pdfBytes),
                pdfBytes.length);
        tradingNote = tradingNote.withFileReference(fileReference);

        log.debug("Persisting note #{}", tradingNote.getNoteNumber());
        TradingNote saved = repository.save(tradingNote);

        log.info("Trading note processed: noteId={}, note=#{}, broker={}",
                saved.getId(), saved.getNoteNumber(), saved.getBroker().getName());
        eventPublisher.publishCreated(TradingNoteCreatedEvent.from(saved));

        return saved;
    }

    private byte[] readBytes(InputStream content) {
        try {
            return content.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF content", e);
        }
    }

    private String computeHash(byte[] content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
