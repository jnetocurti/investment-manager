package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.shared.domain.Entity;
import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static TradingNoteItemBuilder builder() {
        return new CustomTradingNoteItemBuilder();
    }

    public MonetaryValue getNetAmount() {
        return unitPrice.multiply(quantity);
    }

    public MonetaryValue getTotalAmount() {
        return getNetAmount().add(costs);
    }

    static class CustomTradingNoteItemBuilder extends TradingNoteItemBuilder {
        public TradingNoteItem build() {
            var tradingNoteItem = super.build();
            tradingNoteItem.validate();

            return tradingNoteItem;
        }
    }
}
