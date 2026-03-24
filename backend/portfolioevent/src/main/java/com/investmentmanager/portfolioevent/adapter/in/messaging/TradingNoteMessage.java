package com.investmentmanager.portfolioevent.adapter.in.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingNoteMessage {

    private String tradingNoteId;
    private String noteNumber;
    private String brokerName;
    private LocalDate tradingDate;
    private List<OperationMessage> operations;
    private BigDecimal totalNote;
    private BigDecimal totalFees;
    private String currency;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationMessage {
        private String assetName;
        private String operationType;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
        private BigDecimal fee;
    }
}
