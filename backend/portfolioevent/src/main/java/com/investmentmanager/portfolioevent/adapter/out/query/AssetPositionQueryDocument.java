package com.investmentmanager.portfolioevent.adapter.out.query;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "asset_positions")
class AssetPositionQueryDocument {

    @Id
    private String id;
    private String assetName;
    private String assetType;
    private String brokerKey;
    private int quantity;
    private BigDecimal averagePrice;
    private BigDecimal totalCost;
}
