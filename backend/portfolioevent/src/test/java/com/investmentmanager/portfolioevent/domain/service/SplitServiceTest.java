package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSplitCommand;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SplitServiceTest {

    private PortfolioEventRepositoryPort repository;
    private PositionImpactGenerationService impactGenerationService;
    private CanonicalBrokerResolver brokerResolver;
    private SplitService service;

    @BeforeEach
    void setUp() {
        repository = mock(PortfolioEventRepositoryPort.class);
        impactGenerationService = mock(PositionImpactGenerationService.class);
        brokerResolver = mock(CanonicalBrokerResolver.class);
        service = new SplitService(repository, impactGenerationService, brokerResolver);

        when(brokerResolver.findOrCreateCanonicalBroker(any())).thenReturn(CanonicalBroker.builder()
                .brokerKey("BROKER_CLEAR")
                .knownNames(Set.of("Clear"))
                .knownDocuments(Set.of("02.332.886/0001-04"))
                .build());
    }

    @Test
    void shouldCreateSplitEvent() {
        when(repository.existsByIdempotencyKey(any())).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(inv -> {
            PortfolioEvent unsaved = inv.<List<PortfolioEvent>>getArgument(0).getFirst();
            return List.of(unsaved.toBuilder().id("split-1").build());
        });
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        PortfolioEvent result = service.create(command("1:10"));

        assertEquals("split-1", result.getId());
        assertEquals(EventType.SPLIT, result.getEventType());
        assertEquals("1:10", result.getMetadata().getSplitRatio());
        assertEquals("PENDING_SETTLEMENT", result.getMetadata().getSplitFractionFlowStatus());
        assertEquals("SPLIT:PETR4:2026-03-31:1:10", result.getMetadata().getSplitFractionSourceReferenceId());
        assertEquals(BigDecimal.ZERO, result.getUnitPrice().toDisplayValue());
    }

    @Test
    void shouldRejectInvalidRatio() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(command("abc")));
        assertTrue(ex.getMessage().contains("Proporção"));
    }

    private CreateSplitCommand command(String ratio) {
        return CreateSplitCommand.builder()
                .targetTicker("PETR4")
                .targetAssetType(AssetType.STOCKS_BRL)
                .ratio(ratio)
                .eventDate(LocalDate.of(2026, 3, 31))
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .currency("BRL")
                .build();
    }
}
