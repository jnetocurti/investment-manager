package com.investmentmanager.portfolioevent.adapter.out.persistence.impact;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "position_impact_events")
@CompoundIndexes({
        @CompoundIndex(name = "uk_impact_idempotency", def = "{'originalEventId': 1, 'impactType': 1, 'sequence': 1}", unique = true),
        @CompoundIndex(name = "idx_ticker_type_broker_date_seq", def = "{'ticker': 1, 'assetType': 1, 'brokerKey': 1, 'eventDate': 1, 'sequence': 1}")
})
public class PositionImpactEventDocument {

    @Id
    private String id;

    private String originalEventId;
    private String ticker;
    private String assetType;
    private String impactType;
    private int sequence;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal fee;
    private BigDecimal factor;
    private String adjustmentType;
    private LocalDate eventDate;
    private String originType;
    private String sourceType;
    private String brokerKey;
    private String sourceReferenceId;
    private Integer schemaVersion;
    private LocalDateTime createdAt;
}
