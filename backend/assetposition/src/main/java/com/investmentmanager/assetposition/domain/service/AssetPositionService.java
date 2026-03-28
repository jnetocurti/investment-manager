package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.assetposition.domain.service.behavior.PositionBehaviorResolver;
import com.investmentmanager.assetposition.domain.service.behavior.PositionState;
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
    private final PositionBehaviorResolver behaviorResolver;

    @Override
    public AssetPosition calculatePosition(String assetName, AssetType assetType, String brokerDocument) {
        List<PositionImpactData> impacts = impactQueryPort
                .findByTickerAndAssetTypeAndBrokerDocument(assetName, assetType, brokerDocument)
                .stream()
                .toList();

        if (impacts.isEmpty()) {
            log.info("Nenhum impacto encontrado para asset={}, brokerDoc={}", assetName, brokerDocument);
            return null;
        }

        PositionState state = PositionState.empty();
        String latestBrokerName = impacts.getFirst().getBrokerName();
        String previousBrokerName = latestBrokerName;
        List<AssetPositionSnapshot> allSnapshots = new ArrayList<>();

        for (PositionImpactData impact : impacts) {
            String observation = null;
            if (!impact.getBrokerName().equals(previousBrokerName)) {
                observation = "Corretora alterou nome de " + previousBrokerName + " para " + impact.getBrokerName();
                previousBrokerName = impact.getBrokerName();
            }
            latestBrokerName = impact.getBrokerName();

            state = behaviorResolver.resolve(impact).apply(state, impact).newState();

            AssetPositionSnapshot snapshot = AssetPositionSnapshot.builder()
                    .quantity(state.quantity())
                    .averagePrice(state.averagePrice())
                    .totalCost(state.totalCost())
                    .eventDate(impact.getEventDate())
                    .sourceType(impact.getSourceType())
                    .sourceReferenceId(impact.getSourceReferenceId() != null ? impact.getSourceReferenceId()
                            : impact.getOriginalEventId() + ":" + impact.getSequence())
                    .observation(observation)
                    .recordedAt(LocalDateTime.now())
                    .build();
            allSnapshots.add(snapshot);
        }

        AssetType resolvedAssetType = assetType != null ? assetType : impacts.getLast().getAssetType();
        return persistPosition(assetName, brokerDocument, state.quantity(), state.averagePrice(), state.totalCost(),
                latestBrokerName, resolvedAssetType, allSnapshots);
    }

    private AssetPosition persistPosition(String assetName,
                                          String brokerDocument,
                                          int quantity,
                                          MonetaryValue avgPrice,
                                          MonetaryValue totalCost,
                                          String latestBrokerName,
                                          AssetType assetType,
                                          List<AssetPositionSnapshot> allSnapshots) {
        historyRepository.deleteByAssetNameAndBrokerDocument(assetName, brokerDocument);
        historyRepository.saveAll(allSnapshots, assetName, brokerDocument);

        List<AssetPositionSnapshot> last10 = new ArrayList<>(allSnapshots.subList(
                Math.max(0, allSnapshots.size() - 10), allSnapshots.size()));
        Collections.reverse(last10);

        AssetPosition position = positionRepository.findByAssetNameAndAssetTypeAndBrokerDocument(
                        assetName, assetType, brokerDocument)
                .map(existing -> existing.toBuilder()
                        .assetType(assetType)
                        .brokerName(latestBrokerName)
                        .quantity(quantity)
                        .averagePrice(avgPrice)
                        .totalCost(totalCost)
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build())
                .orElse(AssetPosition.builder()
                        .assetName(assetName)
                        .assetType(assetType)
                        .brokerName(latestBrokerName)
                        .brokerDocument(brokerDocument)
                        .quantity(quantity)
                        .averagePrice(avgPrice)
                        .totalCost(totalCost)
                        .currency(assetType.getCurrency())
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build());

        AssetPosition saved = positionRepository.save(position);
        log.info("Posição calculada: asset={}, broker={} ({}), qty={}, avgPrice={}",
                assetName, latestBrokerName, brokerDocument, quantity, avgPrice);
        return saved;
    }
}
