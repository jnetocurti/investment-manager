package br.com.investmentmanager.tradingnote.domain.service;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;
import br.com.investmentmanager.tradingnote.domain.repository.TradingNoteRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradingNoteServiceImpl implements TradingNoteService {

    private final TradingNoteRepository repository;

    private final ApplicationEventPublisher publisher;

    @Override
    public TradingNote create(@NonNull byte[] bytes) {

        var tradingNote = TradingNote.valueOf(bytes);

        repository.save(tradingNote);
        tradingNote.getDomainEvents().forEach(publisher::publishEvent);

        return tradingNote;
    }
}