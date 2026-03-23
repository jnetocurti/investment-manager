package com.investmentmanager.tradingnote.adapter.config;

import com.investmentmanager.tradingnote.adapter.out.parser.PdfTradingNoteParser;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteParserPort;
import com.investmentmanager.tradingnote.domain.port.out.FileStoragePort;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteEventPublisherPort;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteRepositoryPort;
import com.investmentmanager.tradingnote.domain.service.ProcessTradingNoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradingNoteConfig {

    @Bean
    public TradingNoteParserPort tradingNoteParser() {
        return PdfTradingNoteParser.createDefault();
    }

    @Bean
    public ProcessTradingNoteService processTradingNoteService(
            TradingNoteParserPort parser,
            TradingNoteRepositoryPort repository,
            TradingNoteEventPublisherPort eventPublisher,
            FileStoragePort fileStorage) {
        return new ProcessTradingNoteService(parser, repository, eventPublisher, fileStorage);
    }
}
