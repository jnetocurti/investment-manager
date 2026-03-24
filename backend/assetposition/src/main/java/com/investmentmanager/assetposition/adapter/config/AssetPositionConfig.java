package com.investmentmanager.assetposition.adapter.config;

import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PortfolioEventQueryPort;
import com.investmentmanager.assetposition.domain.service.AssetPositionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssetPositionConfig {

    @Bean
    public AssetPositionService assetPositionService(
            PortfolioEventQueryPort eventQueryPort,
            AssetPositionRepositoryPort positionRepository,
            AssetPositionHistoryRepositoryPort historyRepository) {
        return new AssetPositionService(eventQueryPort, positionRepository, historyRepository);
    }
}
