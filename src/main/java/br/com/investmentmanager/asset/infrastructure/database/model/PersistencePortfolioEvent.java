package br.com.investmentmanager.asset.infrastructure.database.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PersistencePortfolioEvent {
    private String invoiceNumber;
    private Operation operation;
    private BigDecimal quantity;
    private LocalDate tradingDate;
    private MonetaryValue averagePurchaseCost;
}
