package com.investmentmanager.assetposition.adapter.config;

import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerRegistryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.assetposition.domain.service.AssetPositionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssetPositionConfig {

    @Bean
    public AssetPositionService assetPositionService(
            PositionImpactQueryPort impactQueryPort,
            AssetPositionRepositoryPort positionRepository,
            AssetPositionHistoryRepositoryPort historyRepository,
            BrokerRegistryRepositoryPort brokerRegistryRepository) {
        return new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerRegistryRepository);
    }
}
