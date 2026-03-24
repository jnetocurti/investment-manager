package com.investmentmanager.assetposition.domain.service;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.assetposition.domain.model.PortfolioEventData;
import com.investmentmanager.assetposition.domain.port.in.CalculateAssetPositionUseCase;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionHistoryRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.AssetPositionRepositoryPort;
import com.investmentmanager.assetposition.domain.port.out.PortfolioEventQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calcula a posição consolidada de um ativo por corretora.
 *
 * <p>Sempre relê todos os eventos do ativo e recalcula do zero,
 * garantindo consistência absoluta.</p>
 */
/**
 * Calcula a posição consolidada de um ativo por corretora (identificada pelo CNPJ).
 *
 * <p>Sempre relê todos os eventos do ativo e recalcula do zero,
 * garantindo consistência absoluta.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class AssetPositionService implements CalculateAssetPositionUseCase {

    private final PortfolioEventQueryPort eventQueryPort;
    private final AssetPositionRepositoryPort positionRepository;
    private final AssetPositionHistoryRepositoryPort historyRepository;

    @Override
    public AssetPosition calculatePosition(String assetName, String brokerDocument) {
        List<PortfolioEventData> events = eventQueryPort
                .findByAssetNameAndBrokerDocumentOrderByEventDate(assetName, brokerDocument);

        if (events.isEmpty()) {
            log.info("Nenhum evento encontrado para asset={}, brokerDoc={}", assetName, brokerDocument);
            return null;
        }

        // Calcular posição do zero com snapshots agrupados por origem
        int quantity = 0;
        MonetaryValue avgPrice = MonetaryValue.zero();
        MonetaryValue totalCost = MonetaryValue.zero();
        String latestBrokerName = events.getFirst().getBrokerName();
        String previousBrokerName = latestBrokerName;
        AssetType assetType = events.getFirst().getAssetType();
        List<AssetPositionSnapshot> allSnapshots = new ArrayList<>();

        for (PortfolioEventData event : events) {
            // Detectar mudança de nome da corretora
            String observation = null;
            if (!event.getBrokerName().equals(previousBrokerName)) {
                observation = "Corretora alterou nome de " + previousBrokerName + " para " + event.getBrokerName();
                log.info("Mudança de nome detectada para brokerDoc={}: {} -> {}",
                        brokerDocument, previousBrokerName, event.getBrokerName());
                previousBrokerName = event.getBrokerName();
            }
            latestBrokerName = event.getBrokerName();

            if ("BUY".equals(event.getEventType())) {
                MonetaryValue eventCost = event.getTotalValue().add(event.getFee());
                totalCost = totalCost.add(eventCost);
                quantity += event.getQuantity();
                avgPrice = totalCost.divide(BigDecimal.valueOf(quantity));
            } else if ("SELL".equals(event.getEventType())) {
                quantity -= event.getQuantity();
                if (quantity <= 0) {
                    if (quantity < 0) {
                        log.warn("Quantidade negativa detectada para asset={}, brokerDoc={}, ajustando para 0",
                                assetName, brokerDocument);
                    }
                    quantity = 0;
                    avgPrice = MonetaryValue.zero();
                    totalCost = MonetaryValue.zero();
                } else {
                    totalCost = avgPrice.multiply(quantity);
                }
            }

            // Snapshot agrupado por sourceType + sourceReferenceId
            String currentSourceRef = event.getSourceReferenceId();
            String currentSourceType = event.getSourceType();
            AssetPositionSnapshot snapshot = AssetPositionSnapshot.builder()
                    .quantity(quantity)
                    .averagePrice(avgPrice)
                    .totalCost(totalCost)
                    .eventDate(event.getEventDate())
                    .sourceType(currentSourceType)
                    .sourceReferenceId(currentSourceRef)
                    .observation(observation)
                    .recordedAt(LocalDateTime.now())
                    .build();

            if (!allSnapshots.isEmpty()
                    && allSnapshots.getLast().getSourceReferenceId().equals(currentSourceRef)
                    && allSnapshots.getLast().getSourceType().equals(currentSourceType)) {
                // Manter observation se o snapshot anterior tinha uma
                String mergedObs = allSnapshots.getLast().getObservation() != null
                        ? allSnapshots.getLast().getObservation() : observation;
                allSnapshots.set(allSnapshots.size() - 1, snapshot.toBuilder()
                        .observation(mergedObs).build());
            } else {
                allSnapshots.add(snapshot);
            }
        }

        // Persistir histórico completo (delete + reinsert)
        historyRepository.deleteByAssetNameAndBrokerDocument(assetName, brokerDocument);
        historyRepository.saveAll(allSnapshots, assetName, brokerDocument);

        // Últimos 10 snapshots (mais recente primeiro)
        List<AssetPositionSnapshot> last10 = new ArrayList<>(allSnapshots.subList(
                Math.max(0, allSnapshots.size() - 10), allSnapshots.size()));
        Collections.reverse(last10);

        final int finalQuantity = quantity;
        final MonetaryValue finalAvgPrice = avgPrice;
        final MonetaryValue finalTotalCost = totalCost;
        final String finalBrokerName = latestBrokerName;

        AssetPosition position = positionRepository.findByAssetNameAndBrokerDocument(assetName, brokerDocument)
                .map(existing -> existing.toBuilder()
                        .brokerName(finalBrokerName)
                        .quantity(finalQuantity)
                        .averagePrice(finalAvgPrice)
                        .totalCost(finalTotalCost)
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build())
                .orElse(AssetPosition.builder()
                        .assetName(assetName)
                        .assetType(assetType)
                        .brokerName(finalBrokerName)
                        .brokerDocument(brokerDocument)
                        .quantity(finalQuantity)
                        .averagePrice(finalAvgPrice)
                        .totalCost(finalTotalCost)
                        .currency("BRL")
                        .updatedAt(LocalDateTime.now())
                        .history(last10)
                        .build());

        AssetPosition saved = positionRepository.save(position);
        log.info("Posição calculada: asset={}, broker={} ({}), qty={}, avgPrice={}",
                assetName, finalBrokerName, brokerDocument, quantity, avgPrice);
        return saved;
    }
}
