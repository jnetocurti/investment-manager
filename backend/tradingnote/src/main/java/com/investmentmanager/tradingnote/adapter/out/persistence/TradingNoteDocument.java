package com.investmentmanager.tradingnote.adapter.out.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "trading_notes")
@CompoundIndexes({
    @CompoundIndex(name = "idx_broker_tradingDate", def = "{'brokerName': 1, 'tradingDate': 1}"),
    @CompoundIndex(name = "idx_broker_noteNumber", def = "{'brokerName': 1, 'noteNumber': 1}")
})
public class TradingNoteDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String fileHash;

    @Indexed
    private String noteNumber;

    private String brokerName;
    private String brokerDocumentId;

    @Indexed
    private LocalDate tradingDate;

    private LocalDate settlementDate;
    private List<OperationDoc> operations;
    private List<FeeDoc> fees;
    private BigDecimal totalNote;
    private BigDecimal netOperations;
    private BigDecimal totalFees;
    private String fileReference;
    private String currency;

    public record OperationDoc(String assetName, String type, int quantity,
                                BigDecimal unitPrice, BigDecimal totalValue, BigDecimal fee) {}

    public record FeeDoc(String description, BigDecimal value) {}
}
