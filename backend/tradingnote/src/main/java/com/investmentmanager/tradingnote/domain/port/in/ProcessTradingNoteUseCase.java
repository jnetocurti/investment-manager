package com.investmentmanager.tradingnote.domain.port.in;

import com.investmentmanager.tradingnote.domain.model.TradingNote;

import java.io.InputStream;

public interface ProcessTradingNoteUseCase {

    TradingNote process(String filename, InputStream pdfContent, long fileSize);
}
