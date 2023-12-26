package br.com.investmentmanager.tradingnote.infrastructure.messagebroker;

import br.com.investmentmanager.tradingnote.TradingNoteRandomizer;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import br.com.investmentmanager.tradingnote.infrastructure.messagebroker.model.TradingNoteMessage;
import org.assertj.core.api.AssertionsForClassTypes;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublishTradingNoteCreatedHandlerImplTest {

    String exchange = "trading-note";
    String createdRoutingKey = "trading-note.event.created";

    @InjectMocks
    private PublishTradingNoteCreatedHandlerImpl handler;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Captor
    private ArgumentCaptor<TradingNoteMessage> captor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "exchange", exchange);
        ReflectionTestUtils.setField(handler, "createdRoutingKey", createdRoutingKey);
    }

    @Test
    void handle() {
        var generator = new EasyRandom(new EasyRandomParameters()
                .randomize(TradingNote.class, new TradingNoteRandomizer()));
        var tradingNoteCreated = new TradingNoteCreated(generator.nextObject(TradingNote.class));
        var tradingNote = tradingNoteCreated.getSource();

        handler.handle(tradingNoteCreated);

        verify(rabbitTemplate, times(1)).convertAndSend(eq(exchange), eq(createdRoutingKey), captor.capture());
        var tradingNoteMessage = captor.getValue();

        assertThat(tradingNoteMessage).usingRecursiveComparison()
                .ignoringFields("netAmount", "totalAmount", "items.netAmount", "items.totalAmount")
                .isEqualTo(tradingNote);

        AssertionsForClassTypes.assertThat(tradingNoteMessage)
                .usingRecursiveComparison()
                .ignoringFields("netAmount", "totalAmount", "items.netAmount", "items.totalAmount")
                .isEqualTo(tradingNote);

        assertEquals(tradingNoteMessage.getNetAmount(), tradingNote.getNetAmount());
        assertEquals(tradingNoteMessage.getTotalAmount(), tradingNote.getTotalAmount());

        var tradingNoteMessageItems = tradingNoteMessage.getItems().stream().toList();
        IntStream.range(0, tradingNoteMessageItems.size()).forEach(i -> {
            assertEquals(tradingNoteMessageItems.get(i).getNetAmount(), tradingNote.getItems().get(i).getNetAmount());
            assertEquals(tradingNoteMessageItems.get(i).getTotalAmount(), tradingNote.getItems()
                    .get(i).getTotalAmount());
        });
    }
}