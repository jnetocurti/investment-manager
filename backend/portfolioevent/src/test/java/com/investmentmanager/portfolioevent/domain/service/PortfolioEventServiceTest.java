package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.OperationType;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreatePortfolioEventsCommand;
import com.investmentmanager.portfolioevent.domain.port.out.AssetDetailResolverPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortfolioEventServiceTest {

    private PortfolioEventRepositoryPort repository;
    private AssetDetailResolverPort assetDetailResolver;
    private PositionImpactGenerationService impactGenerationService;
    private CanonicalBrokerResolver brokerResolver;
    private PortfolioEventService service;

    @BeforeEach
    void setUp() {
        repository = mock(PortfolioEventRepositoryPort.class);
        assetDetailResolver = mock(AssetDetailResolverPort.class);
        impactGenerationService = mock(PositionImpactGenerationService.class);
        brokerResolver = mock(CanonicalBrokerResolver.class);
        service = new PortfolioEventService(repository, assetDetailResolver, impactGenerationService, brokerResolver);

        when(brokerResolver.findOrCreateCanonicalBroker(any())).thenReturn(CanonicalBroker.builder()
                .brokerKey("BROKER_CLEAR")
                .knownNames(Set.of("Clear"))
                .knownDocuments(Set.of("02.332.886/0001-04"))
                .build());
    }

    @Test
    void shouldUseSettlementDateAsEventDateWhenPresent() {
        CreatePortfolioEventsCommand command = CreatePortfolioEventsCommand.builder()
                .tradingNoteId("note-1")
                .noteNumber("123")
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .tradingDate(LocalDate.of(2026, 4, 1))
                .settlementDate(LocalDate.of(2026, 4, 3))
                .currency("BRL")
                .operations(List.of(CreatePortfolioEventsCommand.OperationData.builder()
                        .assetDescription("PETR4")
                        .operationType(OperationType.BUY)
                        .quantity(10)
                        .unitPrice(new BigDecimal("10"))
                        .totalValue(new BigDecimal("100"))
                        .fee(BigDecimal.ZERO)
                        .build()))
                .build();

        when(assetDetailResolver.resolve("PETR4"))
                .thenReturn(new AssetDetailResolverPort.AssetDetail("PETR4", AssetType.STOCKS_BRL));
        when(repository.existsByIdempotencyKey(any())).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        List<PortfolioEvent> events = service.createFromTradingNote(command);

        assertEquals(1, events.size());
        assertEquals(LocalDate.of(2026, 4, 3), events.getFirst().getEventDate());
    }
}
