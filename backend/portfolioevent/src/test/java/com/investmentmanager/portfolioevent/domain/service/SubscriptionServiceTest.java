package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.*;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSubscriptionCommand;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerRegistryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    private PortfolioEventRepositoryPort repository;
    private PositionImpactGenerationService impactGenerationService;
    private BrokerRegistryPort brokerRegistryPort;
    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        repository = mock(PortfolioEventRepositoryPort.class);
        impactGenerationService = mock(PositionImpactGenerationService.class);
        brokerRegistryPort = mock(BrokerRegistryPort.class);
        service = new SubscriptionService(repository, impactGenerationService, brokerRegistryPort);
        when(brokerRegistryPort.resolveOrCreate(any(), any())).thenReturn(BrokerRecord.builder().id("broker-1").brokerKey("CLEAR").build());
    }

    @Test
    void shouldCreateSubscriptionWithMetadata() {
        CreateSubscriptionCommand command = command(LocalDate.of(2026, 3, 30), "PETR4");

        when(repository.saveAll(any())).thenAnswer(inv -> {
            PortfolioEvent unsaved = inv.<List<PortfolioEvent>>getArgument(0).getFirst();
            return List.of(unsaved.toBuilder().id("sub-1").build());
        });
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        PortfolioEvent result = service.create(command);

        assertEquals("sub-1", result.getId());
        assertEquals(EventType.SUBSCRIPTION, result.getEventType());
        assertNotNull(result.getMetadata());
        assertEquals("PETR12", result.getMetadata().getSubscriptionTicker());
        assertEquals("broker-1", result.getBrokerId());
    }

    @Test
    void shouldKeepMetadataOnConversion() {
        PortfolioEvent subscription = PortfolioEvent.builder()
                .id("sub-1")
                .eventType(EventType.SUBSCRIPTION)
                .eventSource(EventSource.SUBSCRIPTION)
                .assetName("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(10)
                .unitPrice(MonetaryValue.of("10"))
                .totalValue(MonetaryValue.of("100"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 3, 30))
                .brokerId("broker-1")
                .sourceReferenceId("ref-1")
                .idempotencyKey("k1")
                .metadata(PortfolioEventMetadata.subscription("PETR12"))
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById("sub-1")).thenReturn(Optional.of(subscription));
        when(repository.existsBySourceReferenceId("sub-1")).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        PortfolioEvent conversion = service.confirmConversion("sub-1", LocalDate.of(2026, 4, 1));

        assertEquals(EventType.SUBSCRIPTION_CONVERSION, conversion.getEventType());
        assertNotNull(conversion.getMetadata());
        assertEquals("PETR12", conversion.getMetadata().getSubscriptionTicker());
    }

    private CreateSubscriptionCommand command(LocalDate subscriptionDate, String targetTicker) {
        return CreateSubscriptionCommand.builder()
                .subscriptionTicker("PETR12")
                .targetTicker(targetTicker)
                .targetAssetType(AssetType.STOCKS_BRL)
                .quantity(100)
                .unitPrice(new BigDecimal("10.00"))
                .totalValue(new BigDecimal("1000.00"))
                .fee(BigDecimal.ZERO)
                .currency("BRL")
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .subscriptionDate(subscriptionDate)
                .build();
    }
}
