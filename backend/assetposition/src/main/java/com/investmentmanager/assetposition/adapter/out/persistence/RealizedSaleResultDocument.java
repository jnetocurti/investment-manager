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
@Document(collection = "realized_sale_result")
@CompoundIndexes({
        @CompoundIndex(name = "uk_sale_result", def = "{'saleOriginalEventId': 1, 'saleSequence': 1, 'brokerKey': 1, 'assetName': 1}", unique = true),
        @CompoundIndex(name = "idx_asset_broker", def = "{'assetName': 1, 'brokerKey': 1}")
})
class RealizedSaleResultDocument {

    @Id
    private String id;
    private String assetName;
    private String assetType;
    private String brokerKey;
    private LocalDate eventDate;
    private Integer eventOrder;
    private String saleSourceType;
    private String saleSourceReferenceId;
    private String saleOriginalEventId;
    private int saleSequence;
    private int quantitySold;
    private BigDecimal averagePriceUsed;
    private BigDecimal unitSalePrice;
    private BigDecimal grossSaleAmount;
    private BigDecimal allocatedOperationalCost;
    private BigDecimal netSaleAmount;
    private BigDecimal costBasisAmount;
    private BigDecimal realizedResultAmount;
    private String resultType;
    private String currency;
    private LocalDateTime recordedAt;
    private int schemaVersion;
}
