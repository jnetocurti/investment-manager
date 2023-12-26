package br.com.investmentmanager.tradingnote.application.usecases;

import br.com.investmentmanager.tradingnote.application.output.TradingNote;
import br.com.investmentmanager.tradingnote.domain.TradingNoteService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTradingNote {

    private final TradingNoteService service;

    public TradingNote execute(@NonNull byte[] bytes) {
        return new ModelMapper().map(service.create(bytes), TradingNote.class);
    }
}
