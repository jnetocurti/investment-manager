package com.investmentmanager.tradingnote.domain.port.out;

import com.investmentmanager.tradingnote.domain.model.TradingNote;

import java.io.InputStream;

public interface TradingNoteParserPort {

    TradingNote parse(InputStream pdfContent);
}
