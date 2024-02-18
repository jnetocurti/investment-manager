package br.com.investmentmanager.portfolioevent.domain;

import lombok.NonNull;

import java.util.List;

public interface PortfolioEventRepository {

    void save(@NonNull List<PortfolioEvent> portfolioEvents);
}
