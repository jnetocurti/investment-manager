package br.com.investmentmanager.tradingnote.domain.validation;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import br.com.investmentmanager.tradingnote.domain.TradingNoteItem;
import br.com.investmentmanager.tradingnote.domain.validation.constraints.TradingNoteConsistentAmount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradingNoteConsistentAmountValidator implements ConstraintValidator<TradingNoteConsistentAmount, TradingNote> {

    @Override
    public boolean isValid(@NonNull TradingNote tradingNote, ConstraintValidatorContext context) {

        if (tradingNote.getNetAmount() != null && tradingNote.getCosts() != null && !tradingNote.getItems().isEmpty()) {

            var totalAmount = tradingNote.getTotalAmount().getValue().setScale(2, RoundingMode.HALF_UP);

            var itemsTotalAmount = tradingNote.getItems().stream().map(TradingNoteItem::getTotalAmount)
                    .map(MonetaryValue::getValue).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            return itemsTotalAmount.equals(totalAmount);
        }

        return false;
    }
}
