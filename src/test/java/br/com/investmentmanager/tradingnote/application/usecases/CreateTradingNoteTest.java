package br.com.investmentmanager.tradingnote.application.usecases;

import br.com.investmentmanager.tradingnote.TradingNoteRandomizer;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.TradingNoteService;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTradingNoteTest {

    @InjectMocks
    private CreateTradingNote useCase;
    @Mock
    private TradingNoteService service;

    @Test
    void execute() {
        var generator = new EasyRandom(new EasyRandomParameters()
                .randomize(TradingNote.class, new TradingNoteRandomizer()));

        var tradingNote = generator.nextObject(TradingNote.class);
        when(service.create(any())).thenReturn(tradingNote);

        var useCaseResult = useCase.execute(new byte[]{});

        assertThat(useCaseResult).usingRecursiveComparison()
                .ignoringFields("netAmount", "totalAmount", "items.netAmount", "items.totalAmount")
                .isEqualTo(tradingNote);

        assertEquals(useCaseResult.getNetAmount(), tradingNote.getNetAmount());
        assertEquals(useCaseResult.getTotalAmount(), tradingNote.getTotalAmount());

        var persistenceItems = useCaseResult.getItems().stream().toList();
        IntStream.range(0, persistenceItems.size()).forEach(i -> {
            assertEquals(persistenceItems.get(i).getNetAmount(), tradingNote.getItems().get(i).getNetAmount());
            assertEquals(persistenceItems.get(i).getTotalAmount(), tradingNote.getItems().get(i).getTotalAmount());
        });
    }
}