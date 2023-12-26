package br.com.investmentmanager.shared.domain.valueobjects;

import br.com.investmentmanager.shared.util.constants.Currency;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value(staticConstructor = "of")
public class MonetaryValue {
    Currency currency;
    BigDecimal value;

    public MonetaryValue add(@NonNull MonetaryValue monetaryValue) {
        return MonetaryValue.of(this.currency, this.value.add(monetaryValue.value));
    }

    public MonetaryValue subtract(@NonNull MonetaryValue monetaryValue) {
        return MonetaryValue.of(this.currency, this.value.subtract(monetaryValue.value));
    }

    public MonetaryValue multiply(@NonNull BigDecimal multiplier) {
        return MonetaryValue.of(this.currency, this.value.multiply(multiplier));
    }
}
