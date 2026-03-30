package com.investmentmanager.portfolioevent.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.OperationType;
import com.investmentmanager.portfolioevent.domain.service.PortfolioEventIdempotencyKeyFactory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private final String brokerId;
    private final String sourceReferenceId;
    private final String idempotencyKey;
    private final PortfolioEventMetadata metadata;
    private final LocalDateTime createdAt;

    public static PortfolioEvent fromOperation(String sourceReferenceId,
                                               String brokerId,
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
                .brokerId(brokerId)
                .sourceReferenceId(sourceReferenceId)
                .createdAt(LocalDateTime.now())
                .build()
                .withIdempotencyKey();

        event.validate();
        return event;
    }

    public PortfolioEvent withIdempotencyKey() {
        return this.toBuilder()
                .idempotencyKey(PortfolioEventIdempotencyKeyFactory.generate(this))
                .build();
    }

    private void validate() {
        if (eventType == null) throw new IllegalArgumentException("EventType is required");
        if (eventSource == null) throw new IllegalArgumentException("EventSource is required");
        if (assetName == null || assetName.isBlank()) throw new IllegalArgumentException("Asset name is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        if (eventDate == null) throw new IllegalArgumentException("Event date is required");
        if (brokerId == null || brokerId.isBlank()) throw new IllegalArgumentException("Broker ID is required");
        if (sourceReferenceId == null || sourceReferenceId.isBlank()) throw new IllegalArgumentException("Source reference ID is required");
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency key is required");
    }
}
