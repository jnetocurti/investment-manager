package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static br.com.investmentmanager.FileUtils.loadFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradingNoteServiceTest {

    @InjectMocks
    private TradingNoteService service;
    @Mock
    private TradingNoteRepository repository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Captor
    private ArgumentCaptor<DomainEvent> eventCaptor;

    @Test
    void create() {
        var tradingNote = service.create(loadFile("trading-notes/Invoice_103226.pdf"));

        assertNotNull(tradingNote);
        verify(repository, times(1)).save(tradingNote);
        verify(publisher, times(2)).publishEvent(eventCaptor.capture());

        var domainEvents = eventCaptor.getAllValues().stream().toList();
        assertEquals(2, domainEvents.size());
        assertEquals(tradingNote.getFile(), domainEvents.get(0).getSource());
        assertEquals(tradingNote, domainEvents.get(1).getSource());
    }
}