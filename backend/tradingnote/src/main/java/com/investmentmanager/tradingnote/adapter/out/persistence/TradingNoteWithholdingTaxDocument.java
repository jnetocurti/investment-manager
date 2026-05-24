package com.investmentmanager.tradingnote.adapter.out.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Document(collection = "trading_note_withholding_taxes")
@CompoundIndexes({
        @CompoundIndex(name = "uk_note_tax_type", def = "{'tradingNoteId': 1, 'taxType': 1}", unique = true)
})
public class TradingNoteWithholdingTaxDocument {

    @Id
    private String id;
    private String tradingNoteId;
    private String noteNumber;
    private String brokerName;
    private String brokerDocument;
    private LocalDate tradingDate;
    private LocalDate settlementDate;
    private String taxType;
    private BigDecimal taxAmount;
    private String currency;
    private boolean unexpectedForBuyOnly;
}
