package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.util.List;

public interface PortfolioEventImpactTranslator {

    boolean supports(PortfolioEvent event);

    List<PositionImpactEvent> translate(PortfolioEvent event);
}
