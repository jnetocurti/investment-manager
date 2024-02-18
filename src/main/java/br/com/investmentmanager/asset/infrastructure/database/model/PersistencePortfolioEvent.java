package br.com.investmentmanager.asset.infrastructure.database.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PersistencePortfolioEvent {
    private BigDecimal quantity;
    private MonetaryValue averagePurchaseCost;
    private Operation operation;
}
