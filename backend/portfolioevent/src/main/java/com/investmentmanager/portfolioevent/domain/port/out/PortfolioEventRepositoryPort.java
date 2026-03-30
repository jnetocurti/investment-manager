package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioEventRepositoryPort {

    List<PortfolioEvent> saveAll(List<PortfolioEvent> portfolioEvents);

    boolean existsBySourceReferenceId(String sourceReferenceId);

    boolean existsSubscriptionByBusinessKey(
            String assetName,
            AssetType assetType,
            String brokerKey,
            LocalDate eventDate);

    Optional<PortfolioEvent> findById(String id);
}
