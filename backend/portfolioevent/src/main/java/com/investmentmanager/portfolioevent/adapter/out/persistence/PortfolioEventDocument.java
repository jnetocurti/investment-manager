package com.investmentmanager.portfolioevent.adapter.out.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "portfolio_events")
@CompoundIndexes({
    @CompoundIndex(name = "idx_asset_date", def = "{'assetName': 1, 'eventDate': 1}"),
    @CompoundIndex(name = "idx_broker_date", def = "{'brokerName': 1, 'eventDate': 1}"),
    @CompoundIndex(
            name = "uk_subscription_idempotency",
            def = "{'eventType': 1, 'assetName': 1, 'assetType': 1, 'brokerKey': 1, 'eventDate': 1}",
            unique = true,
            partialFilter = "{'eventType': 'SUBSCRIPTION'}")
})
public class PortfolioEventDocument {

    @Id
    private String id;

    private String eventType;
    private String eventSource;
    private String assetName;
    private String assetType;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private BigDecimal fee;
    private String currency;
    private LocalDate eventDate;
    private String brokerName;
    private String brokerDocument;
    private String brokerKey;

    @Indexed
    private String sourceReferenceId;

    private MetadataDocument metadata;

    private LocalDateTime createdAt;

    @Data
    public static class MetadataDocument {
        private String subscriptionTicker;
    }
}
