package com.investmentmanager.assetposition.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregate root — posição consolidada de um ativo por corretora.
 * Identificada pelo par {@code assetName} + {@code brokerDocument}.
 * O {@code brokerName} é informativo e atualizado quando muda.
 */
@Getter
@Builder(toBuilder = true)
public class AssetPosition {

    private final String id;
    private final String assetName;
    private final AssetType assetType;
    private final String brokerName;
    private final String brokerDocument;
    private final int quantity;
    private final MonetaryValue averagePrice;
    private final MonetaryValue totalCost;
    private final String currency;
    private final LocalDateTime updatedAt;
    private final List<AssetPositionSnapshot> history;
}
