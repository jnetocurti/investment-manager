package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

public interface PortfolioEventRepositoryPort {

    PortfolioEvent save(PortfolioEvent portfolioEvent);
}
