package br.com.investmentmanager.asset.domain.valueobjects;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value(staticConstructor = "of")
public class PositionHistory {
    LocalDate date;
    BigDecimal quantity;
    MonetaryValue averagePurchaseCost;
}
