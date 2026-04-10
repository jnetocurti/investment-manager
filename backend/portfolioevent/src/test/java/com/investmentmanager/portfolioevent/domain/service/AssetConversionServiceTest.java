package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateAssetConversionCommand;
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

class AssetConversionServiceTest {

    private PortfolioEventRepositoryPort repository;
    private PositionImpactGenerationService impactGenerationService;
    private CanonicalBrokerResolver brokerResolver;
    private AssetPositionQueryPort assetPositionQueryPort;
    private AssetConversionService service;

    @BeforeEach
    void setUp() {
        repository = mock(PortfolioEventRepositoryPort.class);
        impactGenerationService = mock(PositionImpactGenerationService.class);
        brokerResolver = mock(CanonicalBrokerResolver.class);
        assetPositionQueryPort = mock(AssetPositionQueryPort.class);
        service = new AssetConversionService(repository, impactGenerationService, brokerResolver, assetPositionQueryPort);

        when(brokerResolver.findOrCreateCanonicalBroker(any())).thenReturn(CanonicalBroker.builder()
                .brokerKey("BROKER_CLEAR")
                .knownNames(Set.of("Clear"))
                .knownDocuments(Set.of("02.332.886/0001-04"))
                .build());
    }

    @Test
    void shouldCreateAssetConversionWithFractionMetadata() {
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("FOFT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.of(new AssetPositionQueryPort.AssetPositionData(
                        "FOFT11",
                        AssetType.REAL_ESTATE_FUND_BRL,
                        "BROKER_CLEAR",
                        10,
                        MonetaryValue.of("120.036907"),
                        MonetaryValue.of("1200.369071"))));
        when(repository.existsByIdempotencyKey(any())).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(inv -> {
            PortfolioEvent unsaved = inv.<List<PortfolioEvent>>getArgument(0).getFirst();
            return List.of(unsaved.toBuilder().id("conversion-1").build());
        });
        when(impactGenerationService.generateAndPublish(any())).thenReturn(List.of());

        PortfolioEvent result = service.create(baseCommand("1:0.992479"));

        assertEquals("conversion-1", result.getId());
        assertEquals(EventType.ASSET_CONVERSION, result.getEventType());
        assertEquals("FOFT11", result.getMetadata().getOldTicker());
        assertEquals("HFOF11", result.getMetadata().getNewTicker());
        assertEquals("1:0.992479", result.getMetadata().getSplitRatio());
        assertEquals("PENDING_SETTLEMENT", result.getMetadata().getSplitFractionFlowStatus());
        assertEquals("ASSET_CONVERSION:FOFT11:HFOF11:2020-01-02:1:0.992479",
                result.getMetadata().getSplitFractionSourceReferenceId());
        assertEquals("111.850156", result.getMetadata().getSplitFractionResidualBookValue().toPlainString());
    }

    @Test
    void shouldRejectWhenOriginPositionDoesNotExist() {
        when(assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey("FOFT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_CLEAR"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.create(baseCommand("1:0.992479")));
    }

    private CreateAssetConversionCommand baseCommand(String ratio) {
        return CreateAssetConversionCommand.builder()
                .oldTicker("FOFT11")
                .newTicker("HFOF11")
                .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                .ratio(ratio)
                .eventDate(LocalDate.of(2020, 1, 2))
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .currency("BRL")
                .build();
    }
}
