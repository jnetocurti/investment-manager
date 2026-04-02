package com.investmentmanager.portfolioevent.adapter.config;

import com.investmentmanager.portfolioevent.domain.port.out.AssetDetailResolverPort;
import com.investmentmanager.portfolioevent.domain.port.out.AssetPositionQueryPort;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerCatalogRepositoryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventRepositoryPort;
import com.investmentmanager.portfolioevent.domain.service.CanonicalBrokerResolver;
import com.investmentmanager.portfolioevent.domain.service.PortfolioEventService;
import com.investmentmanager.portfolioevent.domain.service.PositionImpactGenerationService;
import com.investmentmanager.portfolioevent.domain.service.SubscriptionService;
import com.investmentmanager.portfolioevent.domain.service.impact.BuyEventImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.PortfolioEventImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.PositionImpactTranslatorRegistry;
import com.investmentmanager.portfolioevent.domain.service.impact.SellEventImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.SplitImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.TickerRenameImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.SubscriptionConversionImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.SubscriptionPendingImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.SplitService;
import com.investmentmanager.portfolioevent.domain.service.TickerRenameService;
import com.investmentmanager.portfolioevent.domain.port.in.TickerRenameUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PortfolioEventConfig {

    @Bean
    public CanonicalBrokerResolver canonicalBrokerResolver(BrokerCatalogRepositoryPort brokerCatalogRepositoryPort) {
        return new CanonicalBrokerResolver(brokerCatalogRepositoryPort);
    }

    @Bean
    public BuyEventImpactTranslator buyEventImpactTranslator() {
        return new BuyEventImpactTranslator();
    }

    @Bean
    public SellEventImpactTranslator sellEventImpactTranslator() {
        return new SellEventImpactTranslator();
    }

    @Bean
    public SubscriptionPendingImpactTranslator subscriptionPendingImpactTranslator() {
        return new SubscriptionPendingImpactTranslator();
    }

    @Bean
    public SubscriptionConversionImpactTranslator subscriptionConversionImpactTranslator() {
        return new SubscriptionConversionImpactTranslator();
    }

    @Bean
    public SplitImpactTranslator splitImpactTranslator() {
        return new SplitImpactTranslator();
    }

    @Bean
    public TickerRenameImpactTranslator tickerRenameImpactTranslator() {
        return new TickerRenameImpactTranslator();
    }

    @Bean
    public PositionImpactTranslatorRegistry positionImpactTranslatorRegistry(
            BuyEventImpactTranslator buyTranslator,
            SellEventImpactTranslator sellTranslator,
            SubscriptionPendingImpactTranslator pendingTranslator,
            SubscriptionConversionImpactTranslator conversionTranslator,
            SplitImpactTranslator splitImpactTranslator,
            TickerRenameImpactTranslator tickerRenameImpactTranslator) {
        return new PositionImpactTranslatorRegistry(List.<PortfolioEventImpactTranslator>of(
                buyTranslator,
                sellTranslator,
                pendingTranslator,
                conversionTranslator,
                splitImpactTranslator,
                tickerRenameImpactTranslator));
    }

    @Bean
    public PositionImpactGenerationService positionImpactGenerationService(
            PositionImpactTranslatorRegistry registry,
            PositionImpactEventRepositoryPort impactRepository,
            PositionImpactEventPublisherPort impactPublisher) {
        return new PositionImpactGenerationService(registry, impactRepository, impactPublisher);
    }

    @Bean
    public PortfolioEventService portfolioEventService(
            PortfolioEventRepositoryPort repository,
            AssetDetailResolverPort assetDetailResolver,
            PositionImpactGenerationService impactGenerationService,
            CanonicalBrokerResolver canonicalBrokerResolver) {
        return new PortfolioEventService(repository, assetDetailResolver, impactGenerationService, canonicalBrokerResolver);
    }

    @Bean
    public SubscriptionService subscriptionService(
            PortfolioEventRepositoryPort repository,
            PositionImpactGenerationService impactGenerationService,
            CanonicalBrokerResolver canonicalBrokerResolver) {
        return new SubscriptionService(repository, impactGenerationService, canonicalBrokerResolver);
    }

    @Bean
    public SplitService splitService(
            PortfolioEventRepositoryPort repository,
            PositionImpactGenerationService impactGenerationService,
            CanonicalBrokerResolver canonicalBrokerResolver) {
        return new SplitService(repository, impactGenerationService, canonicalBrokerResolver);
    }

    @Bean
    public TickerRenameUseCase tickerRenameUseCase(
            PortfolioEventRepositoryPort repository,
            PositionImpactGenerationService impactGenerationService,
            CanonicalBrokerResolver canonicalBrokerResolver,
            AssetPositionQueryPort assetPositionQueryPort) {
        return new TickerRenameService(repository, impactGenerationService, canonicalBrokerResolver, assetPositionQueryPort);
    }
}
