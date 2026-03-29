package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.BrokerRegistry;
import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerRegistryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssetPositionServiceTest {

    @Test
    void shouldCalculateWhenImpactsComeOrderedFromQuery() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerRegistryRepositoryPort brokerRegistryRepository = mock(BrokerRegistryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerRegistryRepository);

        when(brokerRegistryRepository.findByBrokerKey("123")).thenReturn(Optional.empty());
        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerAliases(
                "PETR4", AssetType.STOCKS_BRL, List.of("123"), List.of("XP"))).thenReturn(List.of(
                impact("e2", PositionImpactType.DECREASE, 40, "11", "1", LocalDate.of(2024, 2, 10), "XP", "123", 2),
                impact("e1", PositionImpactType.INCREASE, 100, "10", "1", LocalDate.of(2024, 1, 10), "XP", "123", 1)
        ));
        when(brokerRegistryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("PETR4", AssetType.STOCKS_BRL, "123")).thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "XP", "123");

        assertThat(position).isNotNull();
        assertThat(position.getQuantity()).isEqualTo(60);
        assertThat(position.getCurrency()).isEqualTo("BRL");
    }

    @Test
    void shouldMergeAliasesFromRegistryAndCaptureBrokerChangeObservation() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerRegistryRepositoryPort brokerRegistryRepository = mock(BrokerRegistryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerRegistryRepository);

        BrokerRegistry existingRegistry = BrokerRegistry.builder()
                .brokerKey("CLEAR")
                .currentName("CLEAR CORRETORA")
                .currentDocument("02.332.886/0001-04")
                .knownNames(List.of("CLEAR CORRETORA"))
                .knownDocuments(List.of("02.332.886/0001-04"))
                .updatedAt(LocalDateTime.now())
                .build();

        when(brokerRegistryRepository.findByBrokerKey("CLEAR")).thenReturn(Optional.of(existingRegistry));
        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerAliases(
                "PETR4",
                AssetType.STOCKS_BRL,
                List.of("02.332.886/0001-04", "45.246.575/0001-78"),
                List.of("CLEAR CORRETORA", "CLEAR")))
                .thenReturn(List.of(
                        impact("e1", PositionImpactType.INCREASE, 100, "10", "0", LocalDate.of(2024, 1, 2),
                                "CLEAR CORRETORA", "02.332.886/0001-04", 1),
                        impact("e2", PositionImpactType.DECREASE, 40, "0", "0", LocalDate.of(2024, 2, 2),
                                "CLEAR", "45.246.575/0001-78", 2)
                ));

        when(brokerRegistryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("PETR4", AssetType.STOCKS_BRL, "CLEAR"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "CLEAR", "45.246.575/0001-78");

        assertThat(position.getBrokerKey()).isEqualTo("CLEAR");
        assertThat(position.getBrokerDocument()).isEqualTo("45.246.575/0001-78");

        verify(historyRepository).saveAll(argThat(history ->
                history.stream().anyMatch(snapshot -> snapshot.getObservation() != null
                        && snapshot.getObservation().contains("Cadastro da corretora atualizado"))),
                eq("PETR4"),
                eq("CLEAR"));

        verify(brokerRegistryRepository).save(argThat(saved ->
                saved.getKnownDocuments().containsAll(List.of("02.332.886/0001-04", "45.246.575/0001-78"))
                        && saved.getKnownNames().containsAll(List.of("CLEAR CORRETORA", "CLEAR"))));
    }

    @Test
    void shouldReplayDeterministically() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerRegistryRepositoryPort brokerRegistryRepository = mock(BrokerRegistryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerRegistryRepository);

        List<PositionImpactData> replaySet = List.of(
                impact("e1", PositionImpactType.INCREASE, 100, "10", "0", LocalDate.of(2024, 1, 10), "XP", "123", 1),
                PositionImpactData.builder()
                        .originalEventId("split")
                        .sequence(1)
                        .ticker("ITSA4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.ADJUST)
                        .quantity(0)
                        .unitPrice(MonetaryValue.zero())
                        .fee(MonetaryValue.zero())
                        .factor(BigDecimal.valueOf(2))
                        .eventDate(LocalDate.of(2024, 2, 10))
                        .createdAt(LocalDateTime.of(2024, 2, 10, 10, 0))
                        .sourceType("CORPORATE_ACTION")
                        .brokerName("XP")
                        .brokerDocument("123")
                        .build()
        );

        when(brokerRegistryRepository.findByBrokerKey("123")).thenReturn(Optional.empty());
        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerAliases("ITSA4", AssetType.STOCKS_BRL, List.of("123"), List.of("XP")))
                .thenReturn(replaySet);
        when(brokerRegistryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("ITSA4", AssetType.STOCKS_BRL, "123")).thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var first = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "XP", "123");
        var second = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "XP", "123");

        assertThat(first.getQuantity()).isEqualTo(200);
        assertThat(second.getQuantity()).isEqualTo(200);
        assertThat(first.getAveragePrice().toString()).isEqualTo(second.getAveragePrice().toString());
        verify(historyRepository, times(2)).deleteByAssetNameAndBrokerKey("ITSA4", "123");
    }

    @Test
    void shouldApplySplitAdjustmentWhenAdjustmentTypeIsSplit() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerRegistryRepositoryPort brokerRegistryRepository = mock(BrokerRegistryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerRegistryRepository);

        List<PositionImpactData> replaySet = List.of(
                impact("e1", PositionImpactType.INCREASE, 100, "10", "0",
                        LocalDate.of(2024, 1, 10), "XP", "123", 1),
                PositionImpactData.builder()
                        .originalEventId("split")
                        .sequence(2)
                        .ticker("PETR4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.ADJUST)
                        .adjustmentType(PositionAdjustmentType.SPLIT)
                        .quantity(0)
                        .unitPrice(MonetaryValue.zero())
                        .fee(MonetaryValue.zero())
                        .factor(BigDecimal.valueOf(2))
                        .eventDate(LocalDate.of(2024, 2, 10))
                        .createdAt(LocalDateTime.of(2024, 2, 10, 10, 0))
                        .sourceType("CORPORATE_ACTION")
                        .brokerName("XP")
                        .brokerDocument("123")
                        .build()
        );

        when(brokerRegistryRepository.findByBrokerKey("123")).thenReturn(Optional.empty());
        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerAliases("PETR4", AssetType.STOCKS_BRL, List.of("123"), List.of("XP")))
                .thenReturn(replaySet);
        when(brokerRegistryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("PETR4", AssetType.STOCKS_BRL, "123")).thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "XP", "123");

        assertThat(position.getQuantity()).isEqualTo(200);
        assertThat(position.getAveragePrice().toString()).isEqualTo("5.00");
    }

    @Test
    void shouldReturnNullWhenNoImpactExists() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerRegistryRepositoryPort brokerRegistryRepository = mock(BrokerRegistryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerRegistryRepository);

        when(brokerRegistryRepository.findByBrokerKey("123")).thenReturn(Optional.empty());
        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerAliases("ABEV3", AssetType.STOCKS_BRL, List.of("123"), List.of("XP")))
                .thenReturn(List.of());

        var result = service.calculatePosition("ABEV3", AssetType.STOCKS_BRL, "XP", "123");

        assertThat(result).isNull();
        verifyNoInteractions(positionRepository);
    }

    private PositionImpactData impact(String id,
                                      PositionImpactType type,
                                      int quantity,
                                      String unitPrice,
                                      String fee,
                                      LocalDate eventDate,
                                      String brokerName,
                                      String brokerDocument,
                                      int sequence) {
        return PositionImpactData.builder()
                .originalEventId(id)
                .sequence(sequence)
                .ticker("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .impactType(type)
                .quantity(quantity)
                .unitPrice(MonetaryValue.of(unitPrice))
                .fee(MonetaryValue.of(fee))
                .eventDate(eventDate)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .sourceType("TRADING_NOTE")
                .brokerName(brokerName)
                .brokerDocument(brokerDocument)
                .sourceReferenceId(id + ":" + sequence)
                .build();
    }
}
