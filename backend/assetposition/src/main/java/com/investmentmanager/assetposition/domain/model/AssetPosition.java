package com.investmentmanager.assetposition.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregate root — posição consolidada de um ativo por corretora.
 * Identificada pelo par {@code assetName} + {@code brokerKey}.
 * O {@code brokerName} e {@code brokerDocument} são dados da versão mais recente da corretora.
 */
@Getter
@Builder(toBuilder = true)
public class AssetPosition {

    private final String id;
    private final String assetName;
    private final AssetType assetType;
    private final String brokerKey;
    private final String brokerName;
    private final String brokerDocument;
    private final List<String> brokerNamesHistory;
    private final List<String> brokerDocumentsHistory;
    private final int quantity;
    private final MonetaryValue averagePrice;
    private final MonetaryValue totalCost;
    private final String currency;
    private final LocalDateTime updatedAt;
    private final List<AssetPositionSnapshot> history;
}
