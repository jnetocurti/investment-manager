package com.investmentmanager.portfolioevent.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.OperationType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Aggregate root — registro factual de um evento de portfólio.
 *
 * <p>Armazena "o que aconteceu, quando, quanto e de onde veio" sem interpretar
 * impacto na posição. Todos os valores monetários são positivos (absolutos),
 * exatamente como aparecem na nota de corretagem. O módulo {@code asset}
 * downstream é quem interpreta o {@code eventType} para decidir o efeito.</p>
 */
@Getter
@Builder(toBuilder = true)
public class PortfolioEvent {

    private final String id;
    private final EventType eventType;
    private final EventSource eventSource;
    private final String assetName;
    private final AssetType assetType;
    private final int quantity;
    private final MonetaryValue unitPrice;
    private final MonetaryValue totalValue;
    private final MonetaryValue fee;
    private final String currency;
    private final LocalDate eventDate;
    private final String brokerName;
    private final String brokerDocument;
    private final String sourceReferenceId;
    private final LocalDateTime createdAt;

    /**
     * Factory method para criar um evento a partir de uma operação de nota de negociação.
     * Mapeia {@code OperationType} → {@code EventType} e registra os valores como recebidos
     * (positivos, absolutos).
     */
    public static PortfolioEvent fromOperation(String sourceReferenceId,
                                               String brokerName,
                                               String brokerDocument,
                                               LocalDate eventDate,
                                               String assetName,
                                               AssetType assetType,
                                               OperationType operationType,
                                               int quantity,
                                               BigDecimal unitPrice,
                                               BigDecimal totalValue,
                                               BigDecimal fee,
                                               String currency) {
        PortfolioEvent event = PortfolioEvent.builder()
                .eventType(EventType.valueOf(operationType.name()))
                .eventSource(EventSource.TRADING_NOTE)
                .assetName(assetName)
                .assetType(assetType)
                .quantity(quantity)
                .unitPrice(MonetaryValue.of(unitPrice))
                .totalValue(MonetaryValue.of(totalValue))
                .fee(MonetaryValue.of(fee))
                .currency(currency != null ? currency : "BRL")
                .eventDate(eventDate)
                .brokerName(brokerName)
                .brokerDocument(brokerDocument)
                .sourceReferenceId(sourceReferenceId)
                .createdAt(LocalDateTime.now())
                .build();

        event.validate();
        return event;
    }

    private void validate() {
        if (eventType == null)
            throw new IllegalArgumentException("EventType is required");
        if (eventSource == null)
            throw new IllegalArgumentException("EventSource is required");
        if (assetName == null || assetName.isBlank())
            throw new IllegalArgumentException("Asset name is required");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");
        if (eventDate == null)
            throw new IllegalArgumentException("Event date is required");
        if (sourceReferenceId == null || sourceReferenceId.isBlank())
            throw new IllegalArgumentException("Source reference ID is required");
    }
}
