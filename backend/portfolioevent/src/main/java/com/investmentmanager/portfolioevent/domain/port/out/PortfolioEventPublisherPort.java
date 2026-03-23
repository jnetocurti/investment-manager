package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEventCreatedEvent;

public interface PortfolioEventPublisherPort {

    void publishCreated(PortfolioEventCreatedEvent event);
}
