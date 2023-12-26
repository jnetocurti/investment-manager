package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.tradingnote.domain.factory.TradingNoteFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradingNoteService {

    private final TradingNoteRepository repository;

    private final ApplicationEventPublisher publisher;

    public TradingNote create(@NonNull byte[] bytes) {

        var tradingNote = TradingNoteFactory.createWithTradingNoteFile(bytes);

        repository.save(tradingNote);
        tradingNote.domainEvents().forEach(publisher::publishEvent);

        return tradingNote;
    }
}