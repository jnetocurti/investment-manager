package br.com.investmentmanager.asset.infrastructure.database.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PersistencePositionHistory {
    private LocalDate date;
    private BigDecimal quantity;
    private MonetaryValue averagePurchaseCost;
}
