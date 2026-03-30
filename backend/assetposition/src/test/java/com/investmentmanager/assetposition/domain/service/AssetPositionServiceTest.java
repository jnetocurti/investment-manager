package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.BrokerRegistry;
import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerRegistryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssetPositionServiceTest {

    @Test
    void shouldCalculatePositionByBrokerId() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerRegistryRepositoryPort brokerRegistryRepository = mock(BrokerRegistryRepositoryPort.class);

        AssetPositionService service = new AssetPositionService(
                impactQueryPort, positionRepository, historyRepository, brokerRegistryRepository);

        when(brokerRegistryRepository.findById("broker-1")).thenReturn(Optional.of(BrokerRegistry.builder()
                .id("broker-1")
                .brokerKey("CLEAR")
                .currentName("Clear")
                .currentDocument("02")
                .updatedAt(LocalDateTime.now())
                .build()));

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerId("PETR4", AssetType.STOCKS_BRL, "broker-1"))
                .thenReturn(List.of(PositionImpactData.builder()
                        .originalEventId("e1")
                        .sequence(1)
                        .ticker("PETR4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.INCREASE)
                        .quantity(10)
                        .unitPrice(MonetaryValue.of("10"))
                        .fee(MonetaryValue.zero())
                        .eventDate(LocalDate.of(2024, 1, 1))
                        .sourceType("TRADING_NOTE")
                        .brokerId("broker-1")
                        .sourceReferenceId("note-1")
                        .schemaVersion(1)
                        .createdAt(LocalDateTime.now())
                        .build()));

        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerId("PETR4", AssetType.STOCKS_BRL, "broker-1"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.calculatePosition("PETR4", AssetType.STOCKS_BRL, "broker-1");

        assertThat(result).isNotNull();
        assertThat(result.getBrokerId()).isEqualTo("broker-1");
        assertThat(result.getQuantity()).isEqualTo(10);
    }
}
