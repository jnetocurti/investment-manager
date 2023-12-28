package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNote;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradingNoteService {

    private final ApplicationEventPublisher publisher;

    public void create(@NonNull byte[] bytes) {
        var tradingNote = TradingNote.valueOf(bytes);
        tradingNote.getDomainEvents().forEach(publisher::publishEvent);
    }
}
