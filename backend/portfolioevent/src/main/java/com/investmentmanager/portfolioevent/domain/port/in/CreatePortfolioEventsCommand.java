package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.OperationType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class CreatePortfolioEventsCommand {

    String tradingNoteId;
    String noteNumber;
    String brokerName;
    String brokerDocument;
    LocalDate tradingDate;
    LocalDate settlementDate;
    List<OperationData> operations;
    String currency;

    @Value
    @Builder
    public static class OperationData {
        String assetDescription;
        OperationType operationType;
        int quantity;
        BigDecimal unitPrice;
        BigDecimal totalValue;
        BigDecimal fee;
    }
}
