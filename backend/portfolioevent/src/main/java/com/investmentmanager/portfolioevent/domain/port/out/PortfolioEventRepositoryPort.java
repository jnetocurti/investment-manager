package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

import java.util.List;

public interface PortfolioEventRepositoryPort {

    List<PortfolioEvent> saveAll(List<PortfolioEvent> portfolioEvents);

    boolean existsBySourceReferenceId(String sourceReferenceId);
}
