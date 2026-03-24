package com.investmentmanager.assetposition.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
class AssetPositionSnapshotDocument {

    private int quantity;
    private BigDecimal averagePrice;
    private BigDecimal totalCost;
    private LocalDate eventDate;
    private String sourceType;
    private String sourceReferenceId;
    private String observation;
    private LocalDateTime recordedAt;
}
