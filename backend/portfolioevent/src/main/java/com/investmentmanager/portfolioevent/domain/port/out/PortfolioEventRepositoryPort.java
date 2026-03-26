package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

import java.util.List;
import java.util.Optional;

public interface PortfolioEventRepositoryPort {

    List<PortfolioEvent> saveAll(List<PortfolioEvent> portfolioEvents);

    boolean existsBySourceReferenceId(String sourceReferenceId);

    Optional<PortfolioEvent> findById(String id);
}
