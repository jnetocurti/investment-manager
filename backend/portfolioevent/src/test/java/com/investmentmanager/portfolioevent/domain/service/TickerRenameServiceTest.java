package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateTickerRenameCommand;
import com.investmentmanager.portfolioevent.domain.port.out.AssetPositionQueryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TickerRenameServiceTest {

    private PortfolioEventRepositoryPort repository;
    private PositionImpactGenerationService impactGenerationService;
    private CanonicalBrokerResolver brokerResolver;
    private AssetPositionQueryPort assetPositionQueryPort;
    private TickerRenameService service;

    @BeforeEach
    void setUp() {
        repository = mock(PortfolioEventRepositoryPort.class);
        impactGenerationService = mock(PositionImpactGenerationService.class);
        brokerResolver = mock(CanonicalBrokerResolver.class);
        assetPositionQueryPort = mock(AssetPositionQueryPort.class);
        service = new TickerRenameService(repository, impactGenerationService, brokerResolver, assetPositionQueryPort);

        when(brokerResolver.findOrCreateCanonicalBroker(any())).thenReturn(CanonicalBroker.builder()
                .brokerKey("BROKER_CLEAR")
                .knownNames(Set.of("Clear"))
                .knownDocuments(Set.of("02.332.886/0001-04"))
                .build());
    }

    @Test
    void shouldCreateTickerRenameEvent() {
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("GETT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.of(new AssetPositionQueryPort.AssetPositionData(
                        "GETT11",
                        AssetType.REAL_ESTATE_FUND_BRL,
                        "BROKER_CLEAR",
                        25,
                        MonetaryValue.of("100"),
                        MonetaryValue.of("2500"))));
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("GGRC11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.empty());
        when(repository.existsByIdempotencyKey(any())).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(inv -> {
            PortfolioEvent unsaved = inv.<List<PortfolioEvent>>getArgument(0).getFirst();
            return List.of(unsaved.toBuilder().id("rename-1").build());
        });
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        PortfolioEvent result = service.create(baseCommand());

        assertEquals("rename-1", result.getId());
        assertEquals(EventType.TICKER_RENAME, result.getEventType());
        assertEquals("GETT11", result.getMetadata().getOldTicker());
        assertEquals("GGRC11", result.getMetadata().getNewTicker());
        assertEquals(25, result.getQuantity());
        assertEquals("TICKER_RENAME:GETT11:GGRC11:2026-04-01", result.getSourceReferenceId());
    }

    @Test
    void shouldRejectWhenTargetTickerAlreadyHasActivePosition() {
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("GETT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.of(new AssetPositionQueryPort.AssetPositionData(
                        "GETT11",
                        AssetType.REAL_ESTATE_FUND_BRL,
                        "BROKER_CLEAR",
                        25,
                        MonetaryValue.of("100"),
                        MonetaryValue.of("2500"))));
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("GGRC11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.of(new AssetPositionQueryPort.AssetPositionData(
                        "GGRC11",
                        AssetType.REAL_ESTATE_FUND_BRL,
                        "BROKER_CLEAR",
                        5,
                        MonetaryValue.of("120"),
                        MonetaryValue.of("600"))));

        assertThrows(IllegalStateException.class, () -> service.create(baseCommand()));
    }

    private CreateTickerRenameCommand baseCommand() {
        return CreateTickerRenameCommand.builder()
                .oldTicker("GETT11")
                .newTicker("GGRC11")
                .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                .eventDate(LocalDate.of(2026, 4, 1))
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .currency("BRL")
                .build();
    }
}
