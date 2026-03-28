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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class AssetPositionService implements CalculateAssetPositionUseCase {

    private final PositionImpactQueryPort impactQueryPort;
    private final AssetPositionRepositoryPort positionRepository;
    private final AssetPositionHistoryRepositoryPort historyRepository;
    private final BrokerRegistryRepositoryPort brokerRegistryRepository;

    @Override
    public AssetPosition calculatePosition(String assetName, AssetType assetType, String brokerName, String brokerDocument) {
        BrokerIdentityResolver.BrokerIdentity brokerIdentity = BrokerIdentityResolver.resolve(brokerName, brokerDocument);
        Optional<BrokerRegistry> registry = brokerRegistryRepository.findByBrokerKey(brokerIdentity.getBrokerKey());

        LinkedHashSet<String> brokerDocuments = new LinkedHashSet<>(brokerIdentity.getKnownDocuments());
        LinkedHashSet<String> brokerNames = new LinkedHashSet<>(brokerIdentity.getKnownNames());
        registry.ifPresent(value -> {
            brokerDocuments.addAll(value.getKnownDocuments());
            brokerNames.addAll(value.getKnownNames());
        });

        List<PositionImpactData> impacts = impactQueryPort
                .findByTickerAndAssetTypeAndBrokerAliases(
                        assetName,
                        assetType,
                        new ArrayList<>(brokerDocuments),
                        new ArrayList<>(brokerNames))
                .stream()
                .toList();

        if (impacts.isEmpty()) {
            log.info("Nenhum impacto encontrado para asset={}, brokerKey={}, brokerDocs={}, brokerNames={}",
                    assetName, brokerIdentity.getBrokerKey(), brokerDocuments, brokerNames);
            return null;
        }

        int quantity = 0;
        MonetaryValue avgPrice = MonetaryValue.zero();
        MonetaryValue totalCost = MonetaryValue.zero();
        String latestBrokerName = impacts.getFirst().getBrokerName();
        String latestBrokerDocument = impacts.getFirst().getBrokerDocument();
        String previousBrokerName = latestBrokerName;
        String previousBrokerDocument = latestBrokerDocument;
        List<AssetPositionSnapshot> allSnapshots = new ArrayList<>();

        for (PositionImpactData impact : impacts) {
            String observation = buildBrokerChangeObservation(
                    previousBrokerName,
                    impact.getBrokerName(),
                    previousBrokerDocument,
                    impact.getBrokerDocument());

            previousBrokerName = impact.getBrokerName();
            previousBrokerDocument = impact.getBrokerDocument();
            latestBrokerName = impact.getBrokerName();
            latestBrokerDocument = impact.getBrokerDocument();
            brokerNames.add(impact.getBrokerName());
            brokerDocuments.add(impact.getBrokerDocument());

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

        BrokerRegistry savedRegistry = saveOrUpdateBrokerRegistry(
                registry,
                brokerIdentity.getBrokerKey(),
                latestBrokerName,
                latestBrokerDocument,
                new ArrayList<>(brokerNames),
                new ArrayList<>(brokerDocuments));

        AssetType resolvedAssetType = assetType != null ? assetType : impacts.getLast().getAssetType();
        return persistPosition(assetName, savedRegistry, quantity, avgPrice, totalCost, resolvedAssetType, allSnapshots);
    }

    private String buildBrokerChangeObservation(String previousBrokerName,
                                                String currentBrokerName,
                                                String previousBrokerDocument,
                                                String currentBrokerDocument) {
        boolean nameChanged = previousBrokerName != null && !previousBrokerName.equals(currentBrokerName);
        boolean documentChanged = previousBrokerDocument != null && !previousBrokerDocument.equals(currentBrokerDocument);

        if (nameChanged && documentChanged) {
            return "Cadastro da corretora atualizado: nome " + previousBrokerName + " -> " + currentBrokerName
                    + " e documento " + previousBrokerDocument + " -> " + currentBrokerDocument;
        }
        if (nameChanged) {
            return "Cadastro da corretora atualizado: nome " + previousBrokerName + " -> " + currentBrokerName;
        }
        if (documentChanged) {
            return "Cadastro da corretora atualizado: documento " + previousBrokerDocument + " -> " + currentBrokerDocument;
        }
        return null;
    }

    private BrokerRegistry saveOrUpdateBrokerRegistry(Optional<BrokerRegistry> registry,
                                                      String brokerKey,
                                                      String currentBrokerName,
                                                      String currentBrokerDocument,
                                                      List<String> knownNames,
                                                      List<String> knownDocuments) {
        BrokerRegistry toSave = registry
                .map(existing -> existing.toBuilder()
                        .currentName(currentBrokerName)
                        .currentDocument(currentBrokerDocument)
                        .knownNames(knownNames)
                        .knownDocuments(knownDocuments)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .orElse(BrokerRegistry.builder()
                        .brokerKey(brokerKey)
                        .currentName(currentBrokerName)
                        .currentDocument(currentBrokerDocument)
                        .knownNames(knownNames)
                        .knownDocuments(knownDocuments)
                        .updatedAt(LocalDateTime.now())
                        .build());

        return brokerRegistryRepository.save(toSave);
    }

    private AssetPosition persistPosition(String assetName,
                                          BrokerRegistry brokerRegistry,
                                          int quantity,
                                          MonetaryValue avgPrice,
                                          MonetaryValue totalCost,
                                          AssetType assetType,
                                          List<AssetPositionSnapshot> allSnapshots) {
        historyRepository.deleteByAssetNameAndBrokerKey(assetName, brokerRegistry.getBrokerKey());
        historyRepository.saveAll(allSnapshots, assetName, brokerRegistry.getBrokerKey());

        List<AssetPositionSnapshot> last10 = new ArrayList<>(allSnapshots.subList(
                Math.max(0, allSnapshots.size() - 10), allSnapshots.size()));
        Collections.reverse(last10);

        AssetPosition position = positionRepository.findByAssetNameAndAssetTypeAndBrokerKey(
                        assetName, assetType, brokerRegistry.getBrokerKey())
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
                        .brokerKey(brokerRegistry.getBrokerKey())
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
        log.info("Posição calculada: asset={}, brokerKey={}, brokerAtual={} ({}), qty={}, avgPrice={}",
                assetName, brokerRegistry.getBrokerKey(), brokerRegistry.getCurrentName(),
                brokerRegistry.getCurrentDocument(), quantity, avgPrice);
        return saved;
    }
}
