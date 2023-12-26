package br.com.investmentmanager.tradingnote.domain.events.handler;

import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import br.com.investmentmanager.tradingnote.domain.repository.TradingNoteFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageTradingNoteFileCreatedHandler {

    private final TradingNoteFileRepository repository;

    @EventListener
    public void handleTradingNoteFileCreated(TradingNoteFileCreated event) {
        repository.save(event.getSource());
    }
}
