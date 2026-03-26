package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AssetPositionService implements CalculateAssetPositionUseCase {

    private final PositionImpactQueryPort impactQueryPort;
    private final AssetPositionRepositoryPort positionRepository;
    private final AssetPositionHistoryRepositoryPort historyRepository;

    @Override
    public AssetPosition calculatePosition(String assetName, String brokerDocument) {
        List<PositionImpactData> impacts = impactQueryPort
                .findByTickerAndBrokerDocument(assetName, brokerDocument)
                .stream()
                .sorted(Comparator
                        .comparing(PositionImpactData::getEventDate)
                        .thenComparing(PositionImpactData::getSequence))
                .toList();

        if (impacts.isEmpty()) {
            log.info("Nenhum impacto encontrado para asset={}, brokerDoc={}", assetName, brokerDocument);
            return null;
        }

        int quantity = 0;
        MonetaryValue avgPrice = MonetaryValue.zero();
        MonetaryValue totalCost = MonetaryValue.zero();
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

            if ("INCREASE".equals(impact.getImpactType())) {
                MonetaryValue eventCost = impact.getUnitPrice().multiply(impact.getQuantity()).add(impact.getFee());
                totalCost = totalCost.add(eventCost);
                quantity += impact.getQuantity();
                avgPrice = totalCost.divide(BigDecimal.valueOf(quantity));
            } else if ("DECREASE".equals(impact.getImpactType())) {
                quantity -= impact.getQuantity();
                if (quantity <= 0) {
                    quantity = 0;
                    avgPrice = MonetaryValue.zero();
                    totalCost = MonetaryValue.zero();
                } else {
                    totalCost = avgPrice.multiply(quantity);
                }
            } else if ("ADJUST".equals(impact.getImpactType())) {
                BigDecimal factor = impact.getFactor();
                if (factor != null && factor.compareTo(BigDecimal.ZERO) > 0) {
                    quantity = BigDecimal.valueOf(quantity).multiply(factor).intValue();
                    avgPrice = avgPrice.divide(factor);
                    totalCost = avgPrice.multiply(quantity).add(impact.getFee());
                } else {
                    quantity = impact.getQuantity();
                    avgPrice = impact.getUnitPrice();
                    totalCost = avgPrice.multiply(quantity).add(impact.getFee());
                }
            }

            AssetPositionSnapshot snapshot = AssetPositionSnapshot.builder()
                    .quantity(quantity)
                    .averagePrice(avgPrice)
                    .totalCost(totalCost)
                    .eventDate(impact.getEventDate())
                    .sourceType(impact.getSourceType())
                    .sourceReferenceId(impact.getOriginalEventId() + ":" + impact.getSequence())
                    .observation(observation)
                    .recordedAt(LocalDateTime.now())
                    .build();
            allSnapshots.add(snapshot);
        }

        return persistPosition(assetName, brokerDocument, quantity, avgPrice, totalCost,
                latestBrokerName, AssetType.STOCKS_BRL, allSnapshots);
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

        AssetPosition position = positionRepository.findByAssetNameAndBrokerDocument(assetName, brokerDocument)
                .map(existing -> existing.toBuilder()
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
                        .currency("BRL")
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build());

        AssetPosition saved = positionRepository.save(position);
        log.info("Posição calculada: asset={}, broker={} ({}), qty={}, avgPrice={}",
                assetName, latestBrokerName, brokerDocument, quantity, avgPrice);
        return saved;
    }
}
