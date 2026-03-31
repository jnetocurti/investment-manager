package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerCatalogQueryPort;
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
    void shouldCalculatePositionUsingBrokerKeyOnly() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);
        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP")).thenReturn(java.util.Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort);

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("PETR4", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(List.of(
                        impact("e1", "PETR4", PositionImpactType.INCREASE, 100, "10", "1", LocalDate.of(2024, 1, 10), 1),
                        impact("e2", "PETR4", PositionImpactType.DECREASE, 40, "11", "1", LocalDate.of(2024, 2, 10), 2)
                ));
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("PETR4", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "BROKER_XP");

        assertThat(position).isNotNull();
        assertThat(position.getBrokerKey()).isEqualTo("BROKER_XP");
        assertThat(position.getQuantity()).isEqualTo(60);
    }

    @Test
    void shouldReplayDeterministically() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);
        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP")).thenReturn(java.util.Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository, brokerCatalogQueryPort);

        List<PositionImpactData> replaySet = List.of(
                impact("e1", "ITSA4", PositionImpactType.INCREASE, 100, "10", "0", LocalDate.of(2024, 1, 10), 1),
                PositionImpactData.builder()
                        .originalEventId("split")
                        .sequence(2)
                        .ticker("ITSA4")
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
                        .brokerKey("BROKER_XP")
                        .build()
        );

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("ITSA4", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(replaySet);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("ITSA4", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var first = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "BROKER_XP");
        var second = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "BROKER_XP");

        assertThat(first.getQuantity()).isEqualTo(200);
        assertThat(second.getQuantity()).isEqualTo(200);
    }

    private PositionImpactData impact(String id,
                                      String ticker,
                                      PositionImpactType type,
                                      int quantity,
                                      String unitPrice,
                                      String fee,
                                      LocalDate eventDate,
                                      int sequence) {
        return PositionImpactData.builder()
                .originalEventId(id)
                .sequence(sequence)
                .ticker(ticker)
                .assetType(AssetType.STOCKS_BRL)
                .impactType(type)
                .quantity(quantity)
                .unitPrice(MonetaryValue.of(unitPrice))
                .fee(MonetaryValue.of(fee))
                .eventDate(eventDate)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .sourceType("TRADING_NOTE")
                .brokerKey("BROKER_XP")
                .sourceReferenceId(id + ":" + sequence)
                .build();
    }
}
