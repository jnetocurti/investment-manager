package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

public interface CorporateActionUseCase {
    PortfolioEvent createSplit(CreateCorporateActionCommand command);
    PortfolioEvent createReverseSplit(CreateCorporateActionCommand command);
}
