package br.com.investmentmanager.tradingnote.infrastructure.database;

import br.com.investmentmanager.tradingnote.TradingNoteRandomizer;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.exceptions.TradingNoteAlreadyExistsException;
import br.com.investmentmanager.tradingnote.infrastructure.database.model.PersistenceTradingNote;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingNoteRepositoryImplTest {

    @InjectMocks
    private TradingNoteRepositoryImpl repository;
    @Mock
    private PersistenceTradingNoteDAO tradingNoteDAO;
    @Captor
    private ArgumentCaptor<PersistenceTradingNote> tradingNoteArgumentCaptor;

    @Test
    void save() {
        var generator = new EasyRandom(new EasyRandomParameters()
                .randomize(TradingNote.class, new TradingNoteRandomizer()));

        var tradingNote = generator.nextObject(TradingNote.class);
        repository.save(tradingNote);

        verify(tradingNoteDAO, times(1)).save(tradingNoteArgumentCaptor.capture());
        var persistenceTradingNote = tradingNoteArgumentCaptor.getValue();

        assertThat(persistenceTradingNote).usingRecursiveComparison()
                .ignoringFields("netAmount", "totalAmount", "items.netAmount", "items.totalAmount")
                .isEqualTo(tradingNote);

        assertEquals(persistenceTradingNote.getNetAmount(), tradingNote.getNetAmount());
        assertEquals(persistenceTradingNote.getTotalAmount(), tradingNote.getTotalAmount());

        var persistenceItems = persistenceTradingNote.getItems().stream().toList();
        IntStream.range(0, persistenceItems.size()).forEach(i -> {
            assertEquals(persistenceItems.get(i).getNetAmount(), tradingNote.getItems().get(i).getNetAmount());
            assertEquals(persistenceItems.get(i).getTotalAmount(), tradingNote.getItems().get(i).getTotalAmount());
        });
    }

    @Test
    void saveThrowsTradingNoteAlreadyExists() {
        var generator = new EasyRandom(new EasyRandomParameters()
                .randomize(TradingNote.class, new TradingNoteRandomizer()));

        when(tradingNoteDAO.save(any())).thenThrow(DuplicateKeyException.class);

        var persistenceTradingNote = Optional.of(generator.nextObject(PersistenceTradingNote.class));
        when(tradingNoteDAO.findOne(any())).thenReturn(persistenceTradingNote);

        var exception = assertThrows(TradingNoteAlreadyExistsException.class, () ->
                repository.save(generator.nextObject(TradingNote.class)));

        assertEquals(persistenceTradingNote.get().getId(), exception.getEntityId());
        assertEquals("The trading note already exists", exception.getMessage());
    }
}