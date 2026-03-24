package com.investmentmanager.assetposition.adapter.out.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "asset_position_history")
@CompoundIndexes({
    @CompoundIndex(name = "idx_hist_asset_broker", def = "{'assetName': 1, 'brokerDocument': 1}")
})
class AssetPositionHistoryDocument {

    @Id
    private String id;
    private String assetName;
    private String brokerDocument;
    private int quantity;
    private BigDecimal averagePrice;
    private BigDecimal totalCost;
    private LocalDate eventDate;
    private String sourceType;
    private String sourceReferenceId;
    private String observation;
    private LocalDateTime recordedAt;
}
