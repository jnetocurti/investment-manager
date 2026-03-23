package com.investmentmanager.portfolioevent.domain.model;

import com.investmentmanager.commons.domain.model.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioEvent {

    private String id;
    private String tradingNoteId;
    private String ticker;
    private OperationType type;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalFees;
    private LocalDate eventDate;
    private String brokerName;
}
