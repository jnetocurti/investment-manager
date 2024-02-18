package br.com.investmentmanager.portfolioevent.domain.events;

import br.com.investmentmanager.portfolioevent.domain.PortfolioEvent;
import br.com.investmentmanager.shared.domain.events.DomainEvent;

public class PortfolioEventCreated extends DomainEvent<PortfolioEvent> {

    public PortfolioEventCreated(PortfolioEvent source) {
        super(source);
    }
}
