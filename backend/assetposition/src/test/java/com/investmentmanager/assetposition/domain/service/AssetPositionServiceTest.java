package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
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

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository);

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocuments("PETR4", AssetType.STOCKS_BRL, List.of("123"))).thenReturn(List.of(
                PositionImpactData.builder()
                        .originalEventId("e2")
                        .sequence(2)
                        .ticker("PETR4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.DECREASE)
                        .quantity(40)
                        .unitPrice(MonetaryValue.of("11"))
                        .fee(MonetaryValue.of("1"))
                        .eventDate(LocalDate.of(2024, 2, 10))
                        .createdAt(LocalDateTime.of(2024, 2, 10, 12, 0))
                        .sourceType("TRADING_NOTE")
                        .brokerName("XP")
                        .brokerDocument("123")
                        .build(),
                PositionImpactData.builder()
                        .originalEventId("e1")
                        .sequence(1)
                        .ticker("PETR4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.INCREASE)
                        .quantity(100)
                        .unitPrice(MonetaryValue.of("10"))
                        .fee(MonetaryValue.of("1"))
                        .eventDate(LocalDate.of(2024, 1, 10))
                        .createdAt(LocalDateTime.of(2024, 6, 10, 12, 0))
                        .sourceType("TRADING_NOTE")
                        .brokerName("XP")
                        .brokerDocument("123")
                        .build()
        ));

        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("PETR4", AssetType.STOCKS_BRL, "123")).thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "XP", "123");

        assertThat(position).isNotNull();
        assertThat(position.getQuantity()).isEqualTo(60);
        assertThat(position.getCurrency()).isEqualTo("BRL");
    }

    @Test
    void shouldCalculateUsingCanonicalBrokerKeyForKnownAlias() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository);

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocuments(
                "PETR4", AssetType.STOCKS_BRL, List.of("02.332.886/0001-04", "45.246.575/0001-78")))
                .thenReturn(List.of(
                        PositionImpactData.builder()
                                .originalEventId("e1")
                                .sequence(1)
                                .ticker("PETR4")
                                .assetType(AssetType.STOCKS_BRL)
                                .impactType(PositionImpactType.INCREASE)
                                .quantity(100)
                                .unitPrice(MonetaryValue.of("10"))
                                .fee(MonetaryValue.zero())
                                .eventDate(LocalDate.of(2024, 1, 2))
                                .createdAt(LocalDateTime.of(2024, 1, 2, 10, 0))
                                .sourceType("TRADING_NOTE")
                                .brokerName("CLEAR CORRETORA")
                                .brokerDocument("02.332.886/0001-04")
                                .build(),
                        PositionImpactData.builder()
                                .originalEventId("e2")
                                .sequence(2)
                                .ticker("PETR4")
                                .assetType(AssetType.STOCKS_BRL)
                                .impactType(PositionImpactType.DECREASE)
                                .quantity(40)
                                .unitPrice(MonetaryValue.zero())
                                .fee(MonetaryValue.zero())
                                .eventDate(LocalDate.of(2024, 2, 2))
                                .createdAt(LocalDateTime.of(2024, 2, 2, 10, 0))
                                .sourceType("TRADING_NOTE")
                                .brokerName("CLEAR")
                                .brokerDocument("45.246.575/0001-78")
                                .build()
                ));

        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("PETR4", AssetType.STOCKS_BRL, "CLEAR"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "CLEAR", "45.246.575/0001-78");

        assertThat(position.getBrokerKey()).isEqualTo("CLEAR");
        assertThat(position.getBrokerDocument()).isEqualTo("45.246.575/0001-78");
        assertThat(position.getBrokerNamesHistory()).containsExactly("CLEAR CORRETORA", "CLEAR");
        assertThat(position.getBrokerDocumentsHistory()).containsExactly("02.332.886/0001-04", "45.246.575/0001-78");
        verify(historyRepository).deleteByAssetNameAndBrokerKey("PETR4", "CLEAR");
    }

    @Test
    void shouldReplayDeterministically() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository);

        List<PositionImpactData> replaySet = List.of(
                PositionImpactData.builder()
                        .originalEventId("e1")
                        .sequence(1)
                        .ticker("ITSA4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.INCREASE)
                        .quantity(100)
                        .unitPrice(MonetaryValue.of("10"))
                        .fee(MonetaryValue.zero())
                        .eventDate(LocalDate.of(2024, 1, 10))
                        .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                        .sourceType("TRADING_NOTE")
                        .brokerName("XP")
                        .brokerDocument("123")
                        .build(),
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

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocuments("ITSA4", AssetType.STOCKS_BRL, List.of("123"))).thenReturn(replaySet);
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
    void shouldReturnNullWhenNoImpactExists() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository);

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocuments("ABEV3", AssetType.STOCKS_BRL, List.of("123"))).thenReturn(List.of());

        var result = service.calculatePosition("ABEV3", AssetType.STOCKS_BRL, "XP", "123");

        assertThat(result).isNull();
        verifyNoInteractions(positionRepository);
    }

}
