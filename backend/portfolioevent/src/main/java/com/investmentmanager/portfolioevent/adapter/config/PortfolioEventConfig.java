package com.investmentmanager.portfolioevent.adapter.config;

import com.investmentmanager.portfolioevent.domain.port.out.AssetDetailResolverPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventRepositoryPort;
import com.investmentmanager.portfolioevent.domain.service.CorporateActionService;
import com.investmentmanager.portfolioevent.domain.service.PortfolioEventService;
import com.investmentmanager.portfolioevent.domain.service.PositionImpactGenerationService;
import com.investmentmanager.portfolioevent.domain.service.SubscriptionService;
import com.investmentmanager.portfolioevent.domain.service.impact.BuyEventImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.PortfolioEventImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.PositionImpactTranslatorRegistry;
import com.investmentmanager.portfolioevent.domain.service.impact.SellEventImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.SubscriptionConversionImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.SubscriptionPendingImpactTranslator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PortfolioEventConfig {

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
    public PositionImpactTranslatorRegistry positionImpactTranslatorRegistry(
            BuyEventImpactTranslator buyTranslator,
            SellEventImpactTranslator sellTranslator,
            SubscriptionPendingImpactTranslator pendingTranslator,
            SubscriptionConversionImpactTranslator conversionTranslator) {
        return new PositionImpactTranslatorRegistry(List.<PortfolioEventImpactTranslator>of(
                buyTranslator,
                sellTranslator,
                pendingTranslator,
                conversionTranslator));
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
            PositionImpactGenerationService impactGenerationService) {
        return new PortfolioEventService(repository, assetDetailResolver, impactGenerationService);
    }

    @Bean
    public SubscriptionService subscriptionService(
            PortfolioEventRepositoryPort repository,
            PositionImpactGenerationService impactGenerationService) {
        return new SubscriptionService(repository, impactGenerationService);
    }

    @Bean
    public CorporateActionService corporateActionService(
            PositionImpactEventRepositoryPort impactRepository,
            PositionImpactEventPublisherPort impactPublisher) {
        return new CorporateActionService(impactRepository, impactPublisher);
    }
}
