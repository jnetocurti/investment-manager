package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

import java.util.List;

public interface CreatePortfolioEventsUseCase {

    List<PortfolioEvent> createFromTradingNote(CreatePortfolioEventsCommand command);
}
