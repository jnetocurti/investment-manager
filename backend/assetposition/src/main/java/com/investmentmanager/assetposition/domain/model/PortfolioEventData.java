package com.investmentmanager.assetposition.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Projeção de leitura de um evento de portfólio.
 * Definido no domínio do assetposition — desacoplado do módulo portfolioevent.
 */
@Getter
@Builder
public class PortfolioEventData {

    private final String id;
    private final String eventType;
    private final String assetName;
    private final AssetType assetType;
    private final String brokerName;
    private final String brokerDocument;
    private final int quantity;
    private final MonetaryValue totalValue;
    private final MonetaryValue fee;
    private final LocalDate eventDate;
    private final String sourceType;
    private final String sourceReferenceId;
}
