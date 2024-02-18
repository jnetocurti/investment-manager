package br.com.investmentmanager.portfolioevent.application.usecases;

import br.com.investmentmanager.portfolioevent.application.input.TradingNoteMessage;
import br.com.investmentmanager.portfolioevent.domain.factory.PortfolioEventFactory;
import br.com.investmentmanager.portfolioevent.domain.PortfolioEventService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateFromTradingNote {

    private final PortfolioEventService service;

    private final PortfolioEventFactory factory;

    public void execute(@NonNull TradingNoteMessage tradingNote) {
        var portfolioEvents = factory.createFromTradingNote(tradingNote);
        service.create(portfolioEvents);
    }
}
