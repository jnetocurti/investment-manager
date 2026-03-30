package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.model.BrokerRegistry;
import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.BrokerRegistryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
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
    private final BrokerRegistryRepositoryPort brokerRegistryRepository;
    private final PositionImpactApplierRegistry impactApplierRegistry;

    public AssetPositionService(PositionImpactQueryPort impactQueryPort,
                                AssetPositionRepositoryPort positionRepository,
                                AssetPositionHistoryRepositoryPort historyRepository,
                                BrokerRegistryRepositoryPort brokerRegistryRepository) {
        this(impactQueryPort, positionRepository, historyRepository, brokerRegistryRepository,
                PositionImpactApplierRegistry.defaultRegistry());
    }

    @Override
    public AssetPosition calculatePosition(String assetName, AssetType assetType, String brokerId) {
        BrokerRegistry registry = brokerRegistryRepository.findById(brokerId)
                .orElseThrow(() -> new IllegalStateException("Broker não encontrado para brokerId=" + brokerId));

        List<PositionImpactData> impacts = impactQueryPort
                .findByTickerAndAssetTypeAndBrokerId(assetName, assetType, brokerId)
                .stream()
                .toList();

        if (impacts.isEmpty()) {
            log.info("Nenhum impacto encontrado para asset={}, brokerId={}", assetName, brokerId);
            return null;
        }

        PositionState state = PositionState.builder()
                .quantity(0)
                .averagePrice(MonetaryValue.zero())
                .totalCost(MonetaryValue.zero())
                .build();

        List<AssetPositionSnapshot> allSnapshots = new ArrayList<>();
        for (PositionImpactData impact : impacts) {
            PositionApplyResult result = impactApplierRegistry.apply(state, impact);
            state = result.getState();

            AssetPositionSnapshot snapshot = AssetPositionSnapshot.builder()
                    .quantity(state.getQuantity())
                    .averagePrice(state.getAveragePrice())
                    .totalCost(state.getTotalCost())
                    .eventDate(impact.getEventDate())
                    .sourceType(impact.getSourceType())
                    .sourceReferenceId(impact.getSourceReferenceId() != null ? impact.getSourceReferenceId()
                            : impact.getOriginalEventId() + ":" + impact.getSequence())
                    .recordedAt(LocalDateTime.now())
                    .build();
            allSnapshots.add(snapshot);
        }

        AssetType resolvedAssetType = assetType != null ? assetType : impacts.getLast().getAssetType();
        return persistPosition(assetName, registry, state.getQuantity(), state.getAveragePrice(), state.getTotalCost(),
                resolvedAssetType, allSnapshots);
    }

    private AssetPosition persistPosition(String assetName,
                                          BrokerRegistry brokerRegistry,
                                          int quantity,
                                          MonetaryValue avgPrice,
                                          MonetaryValue totalCost,
                                          AssetType assetType,
                                          List<AssetPositionSnapshot> allSnapshots) {
        historyRepository.deleteByAssetNameAndBrokerKey(assetName, brokerRegistry.getId());
        historyRepository.saveAll(allSnapshots, assetName, brokerRegistry.getId());

        List<AssetPositionSnapshot> last10 = new ArrayList<>(allSnapshots.subList(
                Math.max(0, allSnapshots.size() - 10), allSnapshots.size()));
        Collections.reverse(last10);

        AssetPosition position = positionRepository.findByAssetNameAndAssetTypeAndBrokerId(
                        assetName, assetType, brokerRegistry.getId())
                .map(existing -> existing.toBuilder()
                        .assetType(assetType)
                        .brokerName(brokerRegistry.getCurrentName())
                        .brokerDocument(brokerRegistry.getCurrentDocument())
                        .quantity(quantity)
                        .averagePrice(avgPrice)
                        .totalCost(totalCost)
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build())
                .orElse(AssetPosition.builder()
                        .assetName(assetName)
                        .assetType(assetType)
                        .brokerId(brokerRegistry.getId())
                        .brokerName(brokerRegistry.getCurrentName())
                        .brokerDocument(brokerRegistry.getCurrentDocument())
                        .quantity(quantity)
                        .averagePrice(avgPrice)
                        .totalCost(totalCost)
                        .currency(assetType.getCurrency())
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build());

        AssetPosition saved = positionRepository.save(position);
        log.info("Posição calculada: asset={}, brokerId={}, brokerAtual={} ({}), qty={}, avgPrice={}",
                assetName, brokerRegistry.getId(), brokerRegistry.getCurrentName(),
                brokerRegistry.getCurrentDocument(), quantity, avgPrice);
        return saved;
    }
}
