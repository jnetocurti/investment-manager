package br.com.investmentmanager.portfolioevent.domain.events.handler;

import br.com.investmentmanager.portfolioevent.domain.events.PortfolioEventCreated;
import br.com.investmentmanager.shared.domain.events.handler.EventHandler;

public interface PublishPortfolioEventCreatedHandler extends EventHandler<PortfolioEventCreated> {
}
