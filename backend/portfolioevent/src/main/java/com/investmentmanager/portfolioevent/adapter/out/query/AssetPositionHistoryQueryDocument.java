package com.investmentmanager.portfolioevent.adapter.out.query;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "asset_position_history")
class AssetPositionHistoryQueryDocument {

    @Id
    private String id;
    private String assetName;
    private String brokerKey;
    private int quantity;
    private LocalDate eventDate;
    private Integer eventOrder;
    private LocalDateTime recordedAt;
}
