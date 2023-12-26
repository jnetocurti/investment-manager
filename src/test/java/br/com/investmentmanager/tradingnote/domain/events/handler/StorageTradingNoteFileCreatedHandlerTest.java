package br.com.investmentmanager.tradingnote.domain.events.handler;

import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import br.com.investmentmanager.tradingnote.domain.repository.TradingNoteFileRepository;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.jeasy.random.FieldPredicates.named;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageTradingNoteFileCreatedHandlerTest {

    @InjectMocks
    private StorageTradingNoteFileCreatedHandler handler;
    @Mock
    private TradingNoteFileRepository repository;

    @Test
    void handleTradingNoteFileCreated() {
        var event = new EasyRandom(new EasyRandomParameters().excludeField(named("source")))
                .nextObject(TradingNoteFileCreated.class);

        handler.handleTradingNoteFileCreated(event);

        verify(repository, times(1)).save(event.getSource());
    }

}