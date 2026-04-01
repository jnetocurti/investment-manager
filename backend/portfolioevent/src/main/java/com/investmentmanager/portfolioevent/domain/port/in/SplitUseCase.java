package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

public interface SplitUseCase {

    PortfolioEvent create(CreateSplitCommand command);
}
