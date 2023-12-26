package br.com.investmentmanager.tradingnote.application.rest.impl;

import br.com.investmentmanager.shared.application.rest.ApiErrorResponse;
import br.com.investmentmanager.tradingnote.application.output.TradingNote;
import br.com.investmentmanager.tradingnote.application.rest.TradingNoteController;
import br.com.investmentmanager.tradingnote.application.usecases.CreateTradingNote;
import br.com.investmentmanager.tradingnote.domain.exceptions.InvalidTradingNoteFileException;
import br.com.investmentmanager.tradingnote.domain.exceptions.UnsupportedTradingNoteContentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static br.com.investmentmanager.shared.application.rest.util.HttpUtils.createEntityLocation;

@RestController
@RequiredArgsConstructor
public class TradingNoteControllerImpl implements TradingNoteController {

    private final CreateTradingNote createTradingNote;

    public ResponseEntity<TradingNote> create(MultipartFile file) throws IOException {
        var tradingNote = createTradingNote.execute(file.getBytes());

        return ResponseEntity.created(createEntityLocation(tradingNote.getId())).body(tradingNote);
    }

    @ExceptionHandler({InvalidTradingNoteFileException.class, UnsupportedTradingNoteContentException.class})
    protected ResponseEntity<ApiErrorResponse> handleBadTequests(Exception ex) {
        var errorResponse = ApiErrorResponse.builder()
                .message("Sorry something went wrong. Check the file sent in the request")
                .details(List.of(Map.of("field", "file", "message", ex.getMessage())))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
