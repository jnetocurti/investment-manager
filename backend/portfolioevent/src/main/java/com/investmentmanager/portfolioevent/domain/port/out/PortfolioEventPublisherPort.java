package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEventsProcessedEvent;

public interface PortfolioEventPublisherPort {

    void publishProcessed(PortfolioEventsProcessedEvent event);
}
