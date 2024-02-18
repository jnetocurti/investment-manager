package br.com.investmentmanager.asset.domain.valueobjects;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
public class PortfolioEvent {
    private final BigDecimal quantity;
    private final MonetaryValue averagePurchaseCost;
    private final Operation operation;
}
