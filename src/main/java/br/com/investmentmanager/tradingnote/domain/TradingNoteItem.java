package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.shared.domain.Entity;
import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@ToString
public class TradingNoteItem extends Entity {
    @NotBlank
    private final String assetCode;
    @NotNull
    private final BigDecimal quantity;
    @NotNull
    private final MonetaryValue unitPrice;
    @NotNull
    private final MonetaryValue costs;
    @NotNull
    private final Operation operation;

    public MonetaryValue getNetAmount() {
        return unitPrice.multiply(quantity);
    }

    public MonetaryValue getTotalAmount() {
        return getNetAmount().add(costs);
    }

    @Builder
    public TradingNoteItem(
            UUID id,
            String assetCode,
            BigDecimal quantity,
            MonetaryValue unitPrice,
            MonetaryValue costs,
            Operation operation
    ) {
        super(id);
        this.assetCode = assetCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.costs = costs;
        this.operation = operation;
        this.validate();
    }
}
