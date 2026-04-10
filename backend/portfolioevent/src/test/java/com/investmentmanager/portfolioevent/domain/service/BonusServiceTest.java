package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateBonusCommand;
import com.investmentmanager.portfolioevent.domain.port.out.AssetPositionQueryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BonusServiceTest {

    private PortfolioEventRepositoryPort repository;
    private PositionImpactGenerationService impactGenerationService;
    private CanonicalBrokerResolver brokerResolver;
    private AssetPositionQueryPort assetPositionQueryPort;
    private BonusService service;

    @BeforeEach
    void setUp() {
        repository = mock(PortfolioEventRepositoryPort.class);
        impactGenerationService = mock(PositionImpactGenerationService.class);
        brokerResolver = mock(CanonicalBrokerResolver.class);
        assetPositionQueryPort = mock(AssetPositionQueryPort.class);
        service = new BonusService(repository, impactGenerationService, brokerResolver, assetPositionQueryPort);

        when(brokerResolver.findOrCreateCanonicalBroker(any())).thenReturn(CanonicalBroker.builder()
                .brokerKey("BROKER_CLEAR")
                .knownNames(Set.of("Clear"))
                .knownDocuments(Set.of("02.332.886/0001-04"))
                .build());
    }

    @Test
    void shouldCreateBonusEventWithQuantityDerivedFromPosition() {
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("BBDC3", AssetType.STOCKS_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.of(new AssetPositionQueryPort.AssetPositionData(
                        "BBDC3",
                        AssetType.STOCKS_BRL,
                        "BROKER_CLEAR",
                        50,
                        MonetaryValue.of("10"),
                        MonetaryValue.of("500"))));
        when(repository.existsByIdempotencyKey(any())).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(inv -> {
            PortfolioEvent unsaved = inv.<List<PortfolioEvent>>getArgument(0).getFirst();
            return List.of(unsaved.toBuilder().id("bonus-1").build());
        });
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        PortfolioEvent result = service.create(command("1:10", new BigDecimal("4.527177676")));

        assertEquals("bonus-1", result.getId());
        assertEquals(EventType.BONUS, result.getEventType());
        assertEquals(5, result.getQuantity());
        assertEquals(new BigDecimal("22.635888380"), result.getTotalValue().toDisplayValue());
        assertEquals("1:10", result.getMetadata().getBonusRatio());
        assertEquals(50, result.getMetadata().getBonusBaseQuantity());
    }

    @Test
    void shouldRejectWhenBonusQuantityIsNotEligible() {
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("BBDC3", AssetType.STOCKS_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.of(new AssetPositionQueryPort.AssetPositionData(
                        "BBDC3",
                        AssetType.STOCKS_BRL,
                        "BROKER_CLEAR",
                        9,
                        MonetaryValue.of("10"),
                        MonetaryValue.of("90"))));

        assertThrows(IllegalStateException.class, () -> service.create(command("1:10", new BigDecimal("4.00"))));
    }

    private CreateBonusCommand command(String ratio, BigDecimal unitPrice) {
        return CreateBonusCommand.builder()
                .targetTicker("BBDC3")
                .targetAssetType(AssetType.STOCKS_BRL)
                .ratio(ratio)
                .unitPrice(unitPrice)
                .eventDate(LocalDate.of(2021, 4, 23))
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .currency("BRL")
                .build();
    }
}
