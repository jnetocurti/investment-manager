package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.util.Collections;
import java.util.List;

public class SubscriptionPendingImpactTranslator implements PortfolioEventImpactTranslator {

    @Override
    public boolean supports(PortfolioEvent event) {
        return EventType.SUBSCRIPTION.equals(event.getEventType());
    }

    @Override
    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        return Collections.emptyList();
    }
}
