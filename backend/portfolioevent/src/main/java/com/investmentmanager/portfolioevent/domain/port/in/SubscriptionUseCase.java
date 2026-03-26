package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

import java.time.LocalDate;

public interface SubscriptionUseCase {

    PortfolioEvent create(CreateSubscriptionCommand command);

    PortfolioEvent confirmConversion(String subscriptionEventId, LocalDate conversionDate);
}
