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

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocument("PETR4", AssetType.STOCKS_BRL, "123")).thenReturn(List.of(
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

        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerDocument("PETR4", AssetType.STOCKS_BRL, "123")).thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "123");

        assertThat(position).isNotNull();
        assertThat(position.getQuantity()).isEqualTo(60);
        assertThat(position.getCurrency()).isEqualTo("BRL");
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

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocument("ITSA4", AssetType.STOCKS_BRL, "123")).thenReturn(replaySet);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerDocument("ITSA4", AssetType.STOCKS_BRL, "123")).thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var first = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "123");
        var second = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "123");

        assertThat(first.getQuantity()).isEqualTo(200);
        assertThat(second.getQuantity()).isEqualTo(200);
        assertThat(first.getAveragePrice().toString()).isEqualTo(second.getAveragePrice().toString());
        verify(historyRepository, times(2)).deleteByAssetNameAndBrokerDocument("ITSA4", "123");
    }

    @Test
    void shouldReturnNullWhenNoImpactExists() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository);

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocument("ABEV3", AssetType.STOCKS_BRL, "123")).thenReturn(List.of());

        var result = service.calculatePosition("ABEV3", AssetType.STOCKS_BRL, "123");

        assertThat(result).isNull();
        verifyNoInteractions(positionRepository);
    }

    @Test
    void shouldHandleComplexRealisticSequenceWithTotalSellAndReentry() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository);

        List<PositionImpactData> impacts = List.of(
                impact("e1", PositionImpactType.INCREASE, 100, "10", "0", null, LocalDate.of(2024, 1, 2), 1),
                impact("e2", PositionImpactType.INCREASE, 50, "12", "1", null, LocalDate.of(2024, 1, 10), 2),
                impact("e3", PositionImpactType.DECREASE, 80, "0", "0", null, LocalDate.of(2024, 1, 15), 3),
                impact("e4", PositionImpactType.INCREASE, 20, "11", "0", null, LocalDate.of(2024, 1, 20), 4),
                impact("e5", PositionImpactType.DECREASE, 90, "0", "0", null, LocalDate.of(2024, 1, 25), 5),
                impact("e6", PositionImpactType.INCREASE, 40, "8", "0", null, LocalDate.of(2024, 1, 30), 6)
        );

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocument("VALE3", AssetType.STOCKS_BRL, "123"))
                .thenReturn(impacts);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerDocument("VALE3", AssetType.STOCKS_BRL, "123"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("VALE3", AssetType.STOCKS_BRL, "123");

        assertThat(position.getQuantity()).isEqualTo(40);
        assertThat(position.getAveragePrice()).isEqualTo(MonetaryValue.of("8"));
        assertThat(position.getTotalCost()).isEqualTo(MonetaryValue.of("320"));
    }

    @Test
    void shouldApplyReverseSplitThenPartialSell() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository);

        List<PositionImpactData> impacts = List.of(
                impact("a1", PositionImpactType.INCREASE, 1000, "1.00", "0", null, LocalDate.of(2024, 3, 1), 1),
                impact("a2", PositionImpactType.INCREASE, 200, "1.10", "0", null, LocalDate.of(2024, 3, 2), 2),
                impact("a3", PositionImpactType.ADJUST, 0, "0", "0", new BigDecimal("0.1"), LocalDate.of(2024, 3, 5), 3),
                impact("a4", PositionImpactType.INCREASE, 30, "14", "0", null, LocalDate.of(2024, 3, 10), 4),
                impact("a5", PositionImpactType.DECREASE, 50, "0", "0", null, LocalDate.of(2024, 3, 11), 5)
        );

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerDocument("ABCB4", AssetType.STOCKS_BRL, "123"))
                .thenReturn(impacts);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerDocument("ABCB4", AssetType.STOCKS_BRL, "123"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("ABCB4", AssetType.STOCKS_BRL, "123");

        assertThat(position.getQuantity()).isEqualTo(100);
        assertThat(position.getAveragePrice()).isEqualTo(MonetaryValue.of("10.933336"));
        assertThat(position.getTotalCost()).isEqualTo(MonetaryValue.of("1093.3336"));
    }

    private PositionImpactData impact(String id,
                                      PositionImpactType type,
                                      int quantity,
                                      String unitPrice,
                                      String fee,
                                      BigDecimal factor,
                                      LocalDate eventDate,
                                      int sequence) {
        return PositionImpactData.builder()
                .originalEventId(id)
                .sequence(sequence)
                .ticker("X")
                .assetType(AssetType.STOCKS_BRL)
                .impactType(type)
                .quantity(quantity)
                .unitPrice(MonetaryValue.of(unitPrice))
                .fee(MonetaryValue.of(fee))
                .factor(factor)
                .eventDate(eventDate)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .sourceType("TRADING_NOTE")
                .brokerName("XP")
                .brokerDocument("123")
                .sourceReferenceId(id + ":" + sequence)
                .build();
    }
}
