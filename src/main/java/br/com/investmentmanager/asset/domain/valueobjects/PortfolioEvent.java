package br.com.investmentmanager.asset.domain.valueobjects;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value(staticConstructor = "of")
public class PortfolioEvent {
    String invoiceNumber;
    Operation operation;
    BigDecimal quantity;
    LocalDate tradingDate;
    MonetaryValue averagePurchaseCost;
}
