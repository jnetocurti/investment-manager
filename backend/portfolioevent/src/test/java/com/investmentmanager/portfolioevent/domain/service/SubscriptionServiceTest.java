package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSubscriptionCommand;
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
    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        repository = mock(PortfolioEventRepositoryPort.class);
        impactGenerationService = mock(PositionImpactGenerationService.class);
        service = new SubscriptionService(repository, impactGenerationService);
    }

    @Test
    void shouldCreateSubscriptionWithMetadata() {
        CreateSubscriptionCommand command = command(LocalDate.of(2026, 3, 30), "PETR4");

        when(repository.existsSubscriptionByBusinessKey(any(), any(), any(), any())).thenReturn(false);
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
        verify(repository, times(1)).existsSubscriptionByBusinessKey(
                eq("PETR4"), eq(AssetType.STOCKS_BRL), anyString(), eq(LocalDate.of(2026, 3, 30)));
    }

    @Test
    void shouldRejectDuplicateSubscriptionByBusinessKey() {
        CreateSubscriptionCommand command = command(LocalDate.of(2026, 3, 30), "PETR4");

        when(repository.existsSubscriptionByBusinessKey(any(), any(), any(), any())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.create(command));
        assertEquals("Subscrição duplicada para a mesma posição e data", ex.getMessage());
        verify(repository, never()).saveAll(any());
        verifyNoInteractions(impactGenerationService);
    }

    @Test
    void shouldAllowCreationWhenDateChanges() {
        CreateSubscriptionCommand day1 = command(LocalDate.of(2026, 3, 30), "PETR4");
        CreateSubscriptionCommand day2 = command(LocalDate.of(2026, 3, 31), "PETR4");

        when(repository.existsSubscriptionByBusinessKey(any(), any(), any(), eq(LocalDate.of(2026, 3, 30))))
                .thenReturn(false);
        when(repository.existsSubscriptionByBusinessKey(any(), any(), any(), eq(LocalDate.of(2026, 3, 31))))
                .thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> service.create(day1));
        assertDoesNotThrow(() -> service.create(day2));
        verify(repository, times(2)).saveAll(any());
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
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .brokerKey("CLEAR")
                .sourceReferenceId("ref-1")
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
