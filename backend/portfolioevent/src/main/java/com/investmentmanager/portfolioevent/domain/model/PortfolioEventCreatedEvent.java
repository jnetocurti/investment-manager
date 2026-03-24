package com.investmentmanager.portfolioevent.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Evento publicado no RabbitMQ quando um {@link PortfolioEvent} é criado.
 * Usa tipos primitivos/BigDecimal para serialização JSON limpa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioEventCreatedEvent {

    private String portfolioEventId;
    private String eventType;
    private String assetName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private BigDecimal fee;
    private String currency;
    private LocalDate eventDate;
    private String brokerName;
    private String sourceReferenceId;

    public static PortfolioEventCreatedEvent from(PortfolioEvent event) {
        return PortfolioEventCreatedEvent.builder()
                .portfolioEventId(event.getId())
                .eventType(event.getEventType().name())
                .assetName(event.getAssetName())
                .quantity(event.getQuantity())
                .unitPrice(event.getUnitPrice().toDisplayValue())
                .totalValue(event.getTotalValue().toDisplayValue())
                .fee(event.getFee().toDisplayValue())
                .currency(event.getCurrency())
                .eventDate(event.getEventDate())
                .brokerName(event.getBrokerName())
                .sourceReferenceId(event.getSourceReferenceId())
                .build();
    }
}
