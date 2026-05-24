package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.model.RealizedResultType;
import com.investmentmanager.assetposition.domain.model.RealizedSaleResult;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerCatalogQueryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.assetposition.domain.port.out.SplitFractionMetadataPort;
import com.investmentmanager.assetposition.domain.port.out.RealizedSaleResultRepositoryPort;
import com.investmentmanager.assetposition.domain.service.impact.PositionApplyResult;
import com.investmentmanager.assetposition.domain.service.impact.PositionImpactApplierRegistry;
import com.investmentmanager.assetposition.domain.service.impact.PositionState;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AssetPositionService implements CalculateAssetPositionUseCase {

    private final PositionImpactQueryPort impactQueryPort;
    private final AssetPositionRepositoryPort positionRepository;
    private final AssetPositionHistoryRepositoryPort historyRepository;
    private final BrokerCatalogQueryPort brokerCatalogQueryPort;
    private final SplitFractionMetadataPort splitFractionMetadataPort;
    private final PositionImpactApplierRegistry impactApplierRegistry;
    private final RealizedSaleResultRepositoryPort realizedSaleResultRepository;

    public AssetPositionService(PositionImpactQueryPort impactQueryPort,
                                AssetPositionRepositoryPort positionRepository,
                                AssetPositionHistoryRepositoryPort historyRepository,
                                BrokerCatalogQueryPort brokerCatalogQueryPort) {
        this(impactQueryPort, positionRepository, historyRepository, brokerCatalogQueryPort,
                (splitEventId, splitFractionResidualBookValue, splitFractionSourceReferenceId) -> {
                },
                PositionImpactApplierRegistry.defaultRegistry(),
                (assetName, brokerKey, results) -> {});
    }

    @Override
    public AssetPosition calculatePosition(String assetName, AssetType assetType, String brokerKey) {
        List<PositionImpactData> impacts = impactQueryPort
                .findByTickerAndAssetTypeAndBrokerKey(assetName, assetType, brokerKey);

        if (impacts.isEmpty()) {
            log.info("Nenhum impacto encontrado para asset={}, brokerKey={}", assetName, brokerKey);
            return null;
        }

        PositionState state = PositionState.builder()
                .quantity(0)
                .averagePrice(MonetaryValue.zero())
                .totalCost(MonetaryValue.zero())
                .build();

        List<AssetPositionSnapshot> allSnapshots = new ArrayList<>();
        List<RealizedSaleResult> realizedResults = new ArrayList<>();

        for (PositionImpactData impact : impacts) {
            PositionState stateBeforeImpact = state;
            if (isSellDecreaseImpact(impact, stateBeforeImpact)) {
                realizedResults.add(buildRealizedSaleResult(assetName, impact, stateBeforeImpact));
            }
            PositionApplyResult result = impactApplierRegistry.apply(state, impact);
            state = result.getState();
            if (result.hasSplitFractionResidualBookValue()) {
                splitFractionMetadataPort.updateSplitFractionMetadata(
                        impact.getOriginalEventId(),
                        result.getSplitFractionResidualBookValue(),
                        resolveSplitFractionSourceReferenceId(impact));
            }

            AssetPositionSnapshot snapshot = AssetPositionSnapshot.builder()
                    .quantity(state.getQuantity())
                    .averagePrice(state.getAveragePrice())
                    .totalCost(state.getTotalCost())
                    .eventDate(impact.getEventDate())
                    .eventOrder(impact.getEventOrder() != null ? impact.getEventOrder() : 1)
                    .sourceType(impact.getSourceType())
                    .sourceReferenceId(impact.getSourceReferenceId() != null ? impact.getSourceReferenceId()
                            : impact.getOriginalEventId() + ":" + impact.getSequence())
                    .recordedAt(LocalDateTime.now())
                    .build();
            allSnapshots.add(snapshot);
        }

        realizedSaleResultRepository.upsertAllByAssetAndBroker(assetName, brokerKey, realizedResults);

        AssetType resolvedAssetType = assetType != null ? assetType : impacts.getLast().getAssetType();
        return persistPosition(assetName, brokerKey, state.getQuantity(), state.getAveragePrice(), state.getTotalCost(),
                resolvedAssetType, allSnapshots);
    }


    private boolean isSellDecreaseImpact(PositionImpactData impact, PositionState stateBeforeImpact) {
        return impact.getImpactType() == com.investmentmanager.commons.domain.model.PositionImpactType.DECREASE
                && "SELL".equalsIgnoreCase(impact.getOriginType())
                && impact.getQuantity() > 0
                && stateBeforeImpact.getQuantity() > 0;
    }

    private RealizedSaleResult buildRealizedSaleResult(String assetName, PositionImpactData impact, PositionState stateBeforeImpact) {
        MonetaryValue grossSaleAmount = impact.getUnitPrice().multiply(impact.getQuantity());
        MonetaryValue allocatedOperationalCost = impact.getFee();
        MonetaryValue netSaleAmount = grossSaleAmount.subtract(allocatedOperationalCost);
        MonetaryValue costBasisAmount = stateBeforeImpact.getAveragePrice().multiply(impact.getQuantity());
        MonetaryValue realizedResultAmount = netSaleAmount.subtract(costBasisAmount);

        RealizedResultType resultType = realizedResultAmount.isPositive()
                ? RealizedResultType.PROFIT
                : realizedResultAmount.isNegative() ? RealizedResultType.LOSS : RealizedResultType.BREAK_EVEN;

        return RealizedSaleResult.builder()
                .assetName(assetName)
                .assetType(impact.getAssetType())
                .brokerKey(impact.getBrokerKey())
                .eventDate(impact.getEventDate())
                .eventOrder(impact.getEventOrder() != null ? impact.getEventOrder() : 1)
                .saleSourceType(impact.getSourceType())
                .saleSourceReferenceId(impact.getSourceReferenceId() != null ? impact.getSourceReferenceId()
                        : impact.getOriginalEventId() + ":" + impact.getSequence())
                .saleOriginalEventId(impact.getOriginalEventId())
                .saleSequence(impact.getSequence())
                .quantitySold(impact.getQuantity())
                .averagePriceUsed(stateBeforeImpact.getAveragePrice())
                .unitSalePrice(impact.getUnitPrice())
                .grossSaleAmount(grossSaleAmount)
                .allocatedOperationalCost(allocatedOperationalCost)
                .netSaleAmount(netSaleAmount)
                .costBasisAmount(costBasisAmount)
                .realizedResultAmount(realizedResultAmount)
                .resultType(resultType)
                .currency(impact.getAssetType().getCurrency())
                .recordedAt(LocalDateTime.now())
                .schemaVersion(1)
                .build();
    }

    private String resolveSplitFractionSourceReferenceId(PositionImpactData impact) {
        if (impact.getSourceReferenceId() != null && !impact.getSourceReferenceId().isBlank()) {
            return impact.getSourceReferenceId();
        }
        return "SPLIT_FRACTION:%s:%s".formatted(impact.getOriginalEventId(), impact.getSequence());
    }

    private AssetPosition persistPosition(String assetName,
                                          String brokerKey,
                                          int quantity,
                                          MonetaryValue avgPrice,
                                          MonetaryValue totalCost,
                                          AssetType assetType,
                                          List<AssetPositionSnapshot> allSnapshots) {
        historyRepository.deleteByAssetNameAndBrokerKey(assetName, brokerKey);
        historyRepository.saveAll(allSnapshots, assetName, brokerKey);

        BrokerCatalogQueryPort.BrokerDisplayData brokerDisplay = brokerCatalogQueryPort.findByBrokerKey(brokerKey)
                .orElse(new BrokerCatalogQueryPort.BrokerDisplayData(null, null));

        List<AssetPositionSnapshot> last10 = new ArrayList<>(allSnapshots.subList(
                Math.max(0, allSnapshots.size() - 10), allSnapshots.size()));
        Collections.reverse(last10);

        AssetPosition position = positionRepository.findByAssetNameAndAssetTypeAndBrokerKey(
                        assetName, assetType, brokerKey)
                .map(existing -> existing.toBuilder()
                        .assetType(assetType)
                        .brokerName(brokerDisplay.brokerName())
                        .brokerDocument(brokerDisplay.brokerDocument())
                        .quantity(quantity)
                        .averagePrice(avgPrice)
                        .totalCost(totalCost)
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build())
                .orElse(AssetPosition.builder()
                        .assetName(assetName)
                        .assetType(assetType)
                        .brokerKey(brokerKey)
                        .brokerName(brokerDisplay.brokerName())
                        .brokerDocument(brokerDisplay.brokerDocument())
                        .quantity(quantity)
                        .averagePrice(avgPrice)
                        .totalCost(totalCost)
                        .currency(assetType.getCurrency())
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build());

        AssetPosition saved = positionRepository.save(position);
        log.info("Posição calculada: asset={}, brokerKey={}, qty={}, avgPrice={}",
                assetName, brokerKey, quantity, avgPrice);
        return saved;
    }
}
