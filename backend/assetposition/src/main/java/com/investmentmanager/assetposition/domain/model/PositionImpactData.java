package com.investmentmanager.assetposition.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.commons.domain.model.adjustment.AdjustmentPayload;
import com.investmentmanager.commons.domain.model.adjustment.AdjustmentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PositionImpactData {

    private final String id;
    private final String originalEventId;
    private final String ticker;
    private final AssetType assetType;
    private final PositionImpactType impactType;
    private final int sequence;
    private final int quantity;
    private final MonetaryValue unitPrice;
    private final MonetaryValue fee;
    private final AdjustmentType adjustmentType;
    private final AdjustmentPayload adjustmentPayload;
    private final LocalDate eventDate;
    private final String originType;
    private final String sourceType;
    private final String brokerName;
    private final String brokerDocument;
    private final String sourceReferenceId;
    private final int schemaVersion;
    private final LocalDateTime createdAt;
}
