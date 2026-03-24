package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEventCreatedEvent;

import java.util.List;

public interface PortfolioEventPublisherPort {

    void publishAllCreated(List<PortfolioEventCreatedEvent> events);
}
