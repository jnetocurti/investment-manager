package br.com.investmentmanager.tradingnote.domain.events.handler;

import br.com.investmentmanager.shared.integration.EventPublisher;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
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
class PublishTradingNoteCreatedHandlerTest {

    @InjectMocks
    private PublishTradingNoteCreatedHandler handler;
    @Mock
    private EventPublisher<TradingNoteCreated> publisher;

    @Test
    void handleTradingNoteCreated() {
        var event = new EasyRandom(new EasyRandomParameters().excludeField(named("source")))
                .nextObject(TradingNoteCreated.class);

        handler.handleTradingNoteCreated(event);

        verify(publisher, times(1)).publish(event);
    }
}