package br.com.investmentmanager.tradingnote.application.rest.impl;

import br.com.investmentmanager.MockRequestContext;
import br.com.investmentmanager.tradingnote.application.output.TradingNote;
import br.com.investmentmanager.tradingnote.application.usecases.CreateTradingNote;
import br.com.investmentmanager.tradingnote.domain.exceptions.InvalidTradingNoteFileException;
import lombok.SneakyThrows;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradingNoteControllerImplTest implements MockRequestContext {

    @InjectMocks
    private TradingNoteControllerImpl controller;
    @Mock
    private CreateTradingNote createTradingNote;

    @SneakyThrows
    @Test
    void create() {
        var generator = new EasyRandom();
        var tradingNote = generator.nextObject(TradingNote.class);
        when(createTradingNote.execute(any())).thenReturn(tradingNote);

        var response = controller.create(Mockito.mock(MultipartFile.class));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(tradingNote);

        var expectedLocation = "http://localhost:8080/entity/" + tradingNote.getId();
        assertEquals(expectedLocation, response.getHeaders().get("location").get(0));
    }

    @Test
    void handleBadTequests() {
        var exception = new InvalidTradingNoteFileException(new RuntimeException());

        var response = controller.handleBadTequests(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unable to process request", response.getBody().getTitle());
        assertEquals("Sorry something went wrong. Check the file sent in the request", response.getBody().getMessage());

        var details = (List) response.getBody().getDetails();
        assertEquals("file", ((Map) details.get(0)).get("field"));
        assertEquals("Invalid trading note file", ((Map) details.get(0)).get("message"));
        assertEquals(1, details.size());
    }
}