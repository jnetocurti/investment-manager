package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerCatalogQueryPort;
import com.investmentmanager.assetposition.domain.port.out.SplitFractionMetadataPort;
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

    @Test
    void shouldApplyReverseSplit() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);
        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP")).thenReturn(java.util.Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository, brokerCatalogQueryPort);

        List<PositionImpactData> replaySet = List.of(
                impact("e1", "ABCB4", PositionImpactType.INCREASE, 100, "10", "0", LocalDate.of(2024, 1, 10), 1),
                PositionImpactData.builder()
                        .originalEventId("reverse-split")
                        .sequence(2)
                        .ticker("ABCB4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.ADJUST)
                        .adjustmentType(PositionAdjustmentType.REVERSE_SPLIT)
                        .quantity(0)
                        .unitPrice(MonetaryValue.zero())
                        .fee(MonetaryValue.zero())
                        .factor(new BigDecimal("0.1"))
                        .eventDate(LocalDate.of(2024, 2, 10))
                        .createdAt(LocalDateTime.of(2024, 2, 10, 10, 0))
                        .sourceType("CORPORATE_ACTION")
                        .brokerKey("BROKER_XP")
                        .build()
        );

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("ABCB4", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(replaySet);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("ABCB4", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("ABCB4", AssetType.STOCKS_BRL, "BROKER_XP");

        assertThat(position.getQuantity()).isEqualTo(10);
    }

    @Test
    void shouldNotUndercountReverseSplitWhenFactorIsDecimalApproximation() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);

        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);
        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP")).thenReturn(java.util.Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(impactQueryPort, positionRepository, historyRepository, brokerCatalogQueryPort);

        List<PositionImpactData> replaySet = List.of(
                impact("e1", "ITSA4", PositionImpactType.INCREASE, 3, "10", "0", LocalDate.of(2024, 1, 10), 1),
                PositionImpactData.builder()
                        .originalEventId("reverse-split-3-1")
                        .sequence(2)
                        .ticker("ITSA4")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.ADJUST)
                        .adjustmentType(PositionAdjustmentType.REVERSE_SPLIT)
                        .quantity(0)
                        .unitPrice(MonetaryValue.zero())
                        .fee(MonetaryValue.zero())
                        .factor(new BigDecimal("0.3333333333333333"))
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

        var position = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "BROKER_XP");

        assertThat(position.getQuantity()).isEqualTo(1);
        assertThat(position.getTotalCost().toBigDecimal()).isEqualByComparingTo("30.000000");
        assertThat(position.getAveragePrice().toBigDecimal()).isEqualByComparingTo("30.000000");
    }

    @Test
    void shouldApplySplitThatGeneratesFractionAndPreserveBookIdentity() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        SplitFractionMetadataPort splitFractionMetadataPort = mock(SplitFractionMetadataPort.class);

        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);
        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP")).thenReturn(java.util.Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort,
                splitFractionMetadataPort,
                com.investmentmanager.assetposition.domain.service.impact.PositionImpactApplierRegistry.defaultRegistry());

        List<PositionImpactData> replaySet = List.of(
                impact("e1", "ITSA4", PositionImpactType.INCREASE, 3, "10", "0", LocalDate.of(2024, 1, 10), 1),
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
                        .factor(new BigDecimal("1.5"))
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

        var position = service.calculatePosition("ITSA4", AssetType.STOCKS_BRL, "BROKER_XP");

        assertThat(position.getQuantity()).isEqualTo(4);
        assertThat(position.getAveragePrice().toBigDecimal()).isEqualByComparingTo("6.666667");
        assertThat(position.getTotalCost().toBigDecimal()).isEqualByComparingTo("26.666666");

        MonetaryValue totalBeforeSplit = MonetaryValue.of("30");
        MonetaryValue totalAfterSplit = position.getTotalCost();
        MonetaryValue residual = MonetaryValue.of("3.333334");
        assertThat(totalAfterSplit.add(residual).toBigDecimal()).isEqualByComparingTo(totalBeforeSplit.toBigDecimal());

        verify(splitFractionMetadataPort).updateSplitFractionMetadata(
                eq("split"),
                eq(residual),
                eq("SPLIT_FRACTION:split:2"));
    }

    @Test
    void shouldProjectOldTickerAsZeroAfterTickerRenameImpacts() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);

        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP"))
                .thenReturn(Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort);

        List<PositionImpactData> impacts = List.of(
                impact("buy-1", "GETT11", PositionImpactType.INCREASE, 25, "100", "0",
                        LocalDate.of(2024, 1, 10), 1),
                PositionImpactData.builder()
                        .originalEventId("rename-1")
                        .sequence(1)
                        .ticker("GETT11")
                        .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                        .impactType(PositionImpactType.DECREASE)
                        .quantity(25)
                        .unitPrice(MonetaryValue.zero())
                        .fee(MonetaryValue.zero())
                        .eventDate(LocalDate.of(2024, 2, 10))
                        .createdAt(LocalDateTime.of(2024, 2, 10, 10, 0))
                        .sourceType("CORPORATE_ACTION")
                        .brokerKey("BROKER_XP")
                        .sourceReferenceId("TICKER_RENAME:GETT11:GGRC11:2024-02-10")
                        .build());

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("GETT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(impacts);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("GETT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("GETT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP");

        assertThat(position).isNotNull();
        assertThat(position.getQuantity()).isEqualTo(0);
        assertThat(position.getAveragePrice().toBigDecimal()).isEqualByComparingTo("0.000000");
        assertThat(position.getTotalCost().toBigDecimal()).isEqualByComparingTo("0.000000");
    }

    @Test
    void shouldProjectNewTickerWithPreservedEconomicsAfterTickerRenameImpacts() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);

        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP"))
                .thenReturn(Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort);

        List<PositionImpactData> impacts = List.of(
                PositionImpactData.builder()
                        .originalEventId("rename-1")
                        .sequence(2)
                        .ticker("GGRC11")
                        .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                        .impactType(PositionImpactType.INCREASE)
                        .quantity(25)
                        .unitPrice(MonetaryValue.of("100"))
                        .fee(MonetaryValue.zero())
                        .eventDate(LocalDate.of(2024, 2, 10))
                        .createdAt(LocalDateTime.of(2024, 2, 10, 10, 0))
                        .sourceType("CORPORATE_ACTION")
                        .brokerKey("BROKER_XP")
                        .sourceReferenceId("TICKER_RENAME:GETT11:GGRC11:2024-02-10")
                        .build());

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("GGRC11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(impacts);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("GGRC11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("GGRC11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP");

        assertThat(position).isNotNull();
        assertThat(position.getQuantity()).isEqualTo(25);
        assertThat(position.getAveragePrice().toBigDecimal()).isEqualByComparingTo("100.000000");
        assertThat(position.getTotalCost().toBigDecimal()).isEqualByComparingTo("2500.000000");
    }

    @Test
    void shouldProjectOriginTickerAsZeroAfterAssetConversionImpacts() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);

        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP"))
                .thenReturn(Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort);

        List<PositionImpactData> impacts = List.of(
                impact("buy-1", "FOFT11", PositionImpactType.INCREASE, 10, "120.036907", "0",
                        LocalDate.of(2019, 12, 15), 1),
                PositionImpactData.builder()
                        .originalEventId("conversion-1")
                        .sequence(1)
                        .ticker("FOFT11")
                        .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                        .impactType(PositionImpactType.DECREASE)
                        .quantity(10)
                        .unitPrice(MonetaryValue.zero())
                        .fee(MonetaryValue.zero())
                        .eventDate(LocalDate.of(2020, 1, 2))
                        .createdAt(LocalDateTime.of(2020, 1, 2, 10, 0))
                        .sourceType("CORPORATE_ACTION")
                        .brokerKey("BROKER_XP")
                        .sourceReferenceId("ASSET_CONVERSION:FOFT11:HFOF11:2020-01-02:1:0.992479")
                        .build());

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("FOFT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(impacts);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("FOFT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("FOFT11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP");

        assertThat(position).isNotNull();
        assertThat(position.getQuantity()).isEqualTo(0);
        assertThat(position.getAveragePrice().toBigDecimal()).isEqualByComparingTo("0.000000");
        assertThat(position.getTotalCost().toBigDecimal()).isEqualByComparingTo("0.000000");
    }

    @Test
    void shouldProjectDestinationTickerWithIntegerConvertedQuantityAfterAssetConversionImpacts() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);

        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP"))
                .thenReturn(Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort);

        List<PositionImpactData> impacts = List.of(
                PositionImpactData.builder()
                        .originalEventId("conversion-1")
                        .sequence(2)
                        .ticker("HFOF11")
                        .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                        .impactType(PositionImpactType.INCREASE)
                        .quantity(9)
                        .unitPrice(MonetaryValue.of("120.946546"))
                        .fee(MonetaryValue.zero())
                        .eventDate(LocalDate.of(2020, 1, 2))
                        .createdAt(LocalDateTime.of(2020, 1, 2, 10, 0))
                        .sourceType("CORPORATE_ACTION")
                        .brokerKey("BROKER_XP")
                        .sourceReferenceId("ASSET_CONVERSION:FOFT11:HFOF11:2020-01-02:1:0.992479")
                        .build());

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("HFOF11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(impacts);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("HFOF11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("HFOF11", AssetType.REAL_ESTATE_FUND_BRL, "BROKER_XP");

        assertThat(position).isNotNull();
        assertThat(position.getQuantity()).isEqualTo(9);
        assertThat(position.getAveragePrice().toBigDecimal()).isEqualByComparingTo("120.946546");
        assertThat(position.getTotalCost().toBigDecimal()).isEqualByComparingTo("1088.518914");
    }

    @Test
    void shouldCalculateBonusQuantityDuringProjectionBasedOnTimelineState() {
        PositionImpactQueryPort impactQueryPort = mock(PositionImpactQueryPort.class);
        AssetPositionRepositoryPort positionRepository = mock(AssetPositionRepositoryPort.class);
        AssetPositionHistoryRepositoryPort historyRepository = mock(AssetPositionHistoryRepositoryPort.class);
        BrokerCatalogQueryPort brokerCatalogQueryPort = mock(BrokerCatalogQueryPort.class);

        when(brokerCatalogQueryPort.findByBrokerKey("BROKER_XP"))
                .thenReturn(Optional.of(new BrokerCatalogQueryPort.BrokerDisplayData("XP", "12")));

        AssetPositionService service = new AssetPositionService(
                impactQueryPort,
                positionRepository,
                historyRepository,
                brokerCatalogQueryPort);

        List<PositionImpactData> impacts = List.of(
                impact("buy-1", "BBDC3", PositionImpactType.INCREASE, 50, "10", "0",
                        LocalDate.of(2021, 4, 1), 1),
                PositionImpactData.builder()
                        .originalEventId("bonus-1")
                        .sequence(1)
                        .ticker("BBDC3")
                        .assetType(AssetType.STOCKS_BRL)
                        .impactType(PositionImpactType.INCREASE)
                        .quantity(1)
                        .unitPrice(MonetaryValue.of("4.527177676"))
                        .fee(MonetaryValue.zero())
                        .factor(new BigDecimal("0.1"))
                        .eventDate(LocalDate.of(2021, 4, 23))
                        .originType("BONUS")
                        .sourceType("CORPORATE_ACTION")
                        .brokerKey("BROKER_XP")
                        .sourceReferenceId("BONUS:BBDC3:2021-04-23:1:10")
                        .createdAt(LocalDateTime.of(2021, 4, 23, 10, 0))
                        .build());

        when(impactQueryPort.findByTickerAndAssetTypeAndBrokerKey("BBDC3", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(impacts);
        when(positionRepository.findByAssetNameAndAssetTypeAndBrokerKey("BBDC3", AssetType.STOCKS_BRL, "BROKER_XP"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var position = service.calculatePosition("BBDC3", AssetType.STOCKS_BRL, "BROKER_XP");

        assertThat(position.getQuantity()).isEqualTo(55);
        assertThat(position.getTotalCost().toBigDecimal()).isEqualByComparingTo("522.635888");
        assertThat(position.getAveragePrice().toBigDecimal()).isEqualByComparingTo("9.502471");
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
