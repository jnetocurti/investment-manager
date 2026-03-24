package com.investmentmanager.portfolioevent.adapter.config;

import com.investmentmanager.portfolioevent.domain.port.out.AssetDetailResolverPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import com.investmentmanager.portfolioevent.domain.service.PortfolioEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortfolioEventConfig {

    @Bean
    public PortfolioEventService portfolioEventService(
            PortfolioEventRepositoryPort repository,
            PortfolioEventPublisherPort publisher,
            AssetDetailResolverPort assetDetailResolver) {
        return new PortfolioEventService(repository, publisher, assetDetailResolver);
    }
}
