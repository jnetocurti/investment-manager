package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PositionImpactQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.BrokerIdentityResolver;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AssetPositionService implements CalculateAssetPositionUseCase {

    private final PositionImpactQueryPort impactQueryPort;
    private final AssetPositionRepositoryPort positionRepository;
    private final AssetPositionHistoryRepositoryPort historyRepository;

    @Override
    public AssetPosition calculatePosition(String assetName, AssetType assetType, String brokerName, String brokerDocument) {
        BrokerIdentityResolver.BrokerIdentity brokerIdentity = BrokerIdentityResolver.resolve(brokerName, brokerDocument);
        List<String> brokerDocuments = new ArrayList<>(brokerIdentity.getKnownDocuments());

        List<PositionImpactData> impacts = impactQueryPort
                .findByTickerAndAssetTypeAndBrokerDocuments(assetName, assetType, brokerDocuments)
                .stream()
                .toList();

        if (impacts.isEmpty()) {
            log.info("Nenhum impacto encontrado para asset={}, brokerKey={}, brokerDocs={}",
                    assetName, brokerIdentity.getBrokerKey(), brokerDocuments);
            return null;
        }

        int quantity = 0;
        MonetaryValue avgPrice = MonetaryValue.zero();
        MonetaryValue totalCost = MonetaryValue.zero();
        String latestBrokerName = impacts.getFirst().getBrokerName();
        String latestBrokerDocument = impacts.getFirst().getBrokerDocument();
        String previousBrokerName = latestBrokerName;
        List<AssetPositionSnapshot> allSnapshots = new ArrayList<>();
        LinkedHashSet<String> brokerNamesSeen = new LinkedHashSet<>();
        LinkedHashSet<String> brokerDocumentsSeen = new LinkedHashSet<>();

        for (PositionImpactData impact : impacts) {
            String observation = null;
            if (!impact.getBrokerName().equals(previousBrokerName)) {
                observation = "Corretora alterou nome de " + previousBrokerName + " para " + impact.getBrokerName();
                previousBrokerName = impact.getBrokerName();
            }
            latestBrokerName = impact.getBrokerName();
            latestBrokerDocument = impact.getBrokerDocument();
            brokerNamesSeen.add(impact.getBrokerName());
            brokerDocumentsSeen.add(impact.getBrokerDocument());

            switch (impact.getImpactType()) {
                case INCREASE -> {
                    MonetaryValue eventCost = impact.getUnitPrice().multiply(impact.getQuantity()).add(impact.getFee());
                    totalCost = totalCost.add(eventCost);
                    quantity += impact.getQuantity();
                    avgPrice = totalCost.divide(BigDecimal.valueOf(quantity));
                }
                case DECREASE -> {
                    quantity -= impact.getQuantity();
                    if (quantity <= 0) {
                        quantity = 0;
                        avgPrice = MonetaryValue.zero();
                        totalCost = MonetaryValue.zero();
                    } else {
                        totalCost = avgPrice.multiply(quantity);
                    }
                }
                case ADJUST -> {
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
            }

            AssetPositionSnapshot snapshot = AssetPositionSnapshot.builder()
                    .quantity(quantity)
                    .averagePrice(avgPrice)
                    .totalCost(totalCost)
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
        return persistPosition(assetName, brokerIdentity.getBrokerKey(), quantity, avgPrice, totalCost,
                latestBrokerName, latestBrokerDocument, resolvedAssetType, allSnapshots,
                new ArrayList<>(brokerNamesSeen), new ArrayList<>(brokerDocumentsSeen));
    }

    private AssetPosition persistPosition(String assetName,
                                          String brokerKey,
                                          int quantity,
                                          MonetaryValue avgPrice,
                                          MonetaryValue totalCost,
                                          String latestBrokerName,
                                          String latestBrokerDocument,
                                          AssetType assetType,
                                          List<AssetPositionSnapshot> allSnapshots,
                                          List<String> brokerNamesHistory,
                                          List<String> brokerDocumentsHistory) {
        historyRepository.deleteByAssetNameAndBrokerKey(assetName, brokerKey);
        historyRepository.saveAll(allSnapshots, assetName, brokerKey);

        List<AssetPositionSnapshot> last10 = new ArrayList<>(allSnapshots.subList(
                Math.max(0, allSnapshots.size() - 10), allSnapshots.size()));
        Collections.reverse(last10);

        AssetPosition position = positionRepository.findByAssetNameAndAssetTypeAndBrokerKey(
                        assetName, assetType, brokerKey)
                .map(existing -> existing.toBuilder()
                        .assetType(assetType)
                        .brokerName(latestBrokerName)
                        .brokerDocument(latestBrokerDocument)
                        .brokerNamesHistory(brokerNamesHistory)
                        .brokerDocumentsHistory(brokerDocumentsHistory)
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
                        .brokerName(latestBrokerName)
                        .brokerDocument(latestBrokerDocument)
                        .brokerNamesHistory(brokerNamesHistory)
                        .brokerDocumentsHistory(brokerDocumentsHistory)
                        .quantity(quantity)
                        .averagePrice(avgPrice)
                        .totalCost(totalCost)
                        .currency(assetType.getCurrency())
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build());

        AssetPosition saved = positionRepository.save(position);
        log.info("Posição calculada: asset={}, brokerKey={}, brokerAtual={} ({}), qty={}, avgPrice={}",
                assetName, brokerKey, latestBrokerName, latestBrokerDocument, quantity, avgPrice);
        return saved;
    }
}
