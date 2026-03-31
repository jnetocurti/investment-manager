package com.investmentmanager.portfolioevent.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class PositionImpactEvent {

    private final String id;
    private final String originalEventId;
    private final String ticker;
    private final AssetType assetType;
    private final PositionImpactType impactType;
    private final int sequence;
    private final int quantity;
    private final MonetaryValue unitPrice;
    private final MonetaryValue fee;
    private final BigDecimal factor;
    private final PositionAdjustmentType adjustmentType;
    private final LocalDate eventDate;
    private final EventType originType;
    private final ImpactSourceType sourceType;
    private final String brokerKey;
    private final String sourceReferenceId;
    @Builder.Default
    private final int schemaVersion = 1;
    private final LocalDateTime createdAt;

    public void validate() {
        if (originalEventId == null || originalEventId.isBlank()) {
            throw new IllegalArgumentException("Original event ID is required");
        }
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker is required");
        }
        if (impactType == null) {
            throw new IllegalArgumentException("Impact type is required");
        }
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be > 0");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (originType == null) {
            throw new IllegalArgumentException("Origin type is required");
        }
        if (sourceType == null) {
            throw new IllegalArgumentException("Source type is required");
        }
        if (brokerKey == null || brokerKey.isBlank()) {
            throw new IllegalArgumentException("Broker key is required");
        }
        if (factor != null && factor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Factor must be > 0 when provided");
        }
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("Schema version must be > 0");
        }
    }
}
