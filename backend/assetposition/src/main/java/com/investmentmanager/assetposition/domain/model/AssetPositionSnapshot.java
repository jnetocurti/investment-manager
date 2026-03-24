package com.investmentmanager.assetposition.domain.model;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Snapshot da posição de um ativo após processamento de um agrupador.
 * Registra quantidade e preço médio naquele ponto no tempo.
 */
@Getter
@Builder(toBuilder = true)
public class AssetPositionSnapshot {

    private final int quantity;
    private final MonetaryValue averagePrice;
    private final MonetaryValue totalCost;
    private final LocalDate eventDate;
    private final String sourceType;
    private final String sourceReferenceId;
    private final String observation;
    private final LocalDateTime recordedAt;
}
