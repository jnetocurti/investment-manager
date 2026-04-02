package com.investmentmanager.assetposition.adapter.config;

import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerCatalogQueryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.assetposition.domain.port.out.SplitFractionMetadataPort;
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
            BrokerCatalogQueryPort brokerCatalogQueryPort,
            SplitFractionMetadataPort splitFractionMetadataPort) {
        return new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort,
                splitFractionMetadataPort,
                com.investmentmanager.assetposition.domain.service.impact.PositionImpactApplierRegistry.defaultRegistry());
    }
}
