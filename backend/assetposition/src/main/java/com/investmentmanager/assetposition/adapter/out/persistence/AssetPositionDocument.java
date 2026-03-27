package com.investmentmanager.assetposition.adapter.out.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "asset_positions")
@CompoundIndexes({
    @CompoundIndex(name = "idx_asset_type_broker", def = "{'assetName': 1, 'assetType': 1, 'brokerDocument': 1}", unique = true)
})
class AssetPositionDocument {

    @Id
    private String id;
    private String assetName;
    private String assetType;
    private String brokerName;
    private String brokerDocument;
    private int quantity;
    private BigDecimal averagePrice;
    private BigDecimal totalCost;
    private String currency;
    private LocalDateTime updatedAt;
    private List<AssetPositionSnapshotDocument> history;
}
