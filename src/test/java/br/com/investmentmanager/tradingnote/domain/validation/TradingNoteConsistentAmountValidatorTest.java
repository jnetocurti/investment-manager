package br.com.investmentmanager.tradingnote.domain.validation;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Currency;
import br.com.investmentmanager.tradingnote.TradingNoteRandomizer;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import jakarta.validation.ConstraintValidatorContext;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

class TradingNoteConsistentAmountValidatorTest {

    private final TradingNote tradingNote = Mockito.mock(TradingNote.class);
    private final ConstraintValidatorContext validatorContext = Mockito.mock(ConstraintValidatorContext.class);

    private final TradingNoteConsistentAmountValidator validator = new TradingNoteConsistentAmountValidator();

    @Test
    void isValidFalse() {
        when(tradingNote.getNetAmount()).thenReturn(MonetaryValue.of(Currency.BRL, BigDecimal.ONE));
        assertFalse(validator.isValid(tradingNote, validatorContext));
        when(tradingNote.getCosts()).thenReturn(MonetaryValue.of(Currency.BRL, BigDecimal.ONE));
        assertFalse(validator.isValid(tradingNote, validatorContext));
        when(tradingNote.getItems()).thenReturn(List.of());
        assertFalse(validator.isValid(tradingNote, validatorContext));
    }

    @Test
    void isValidTrue() {
        var generator = new EasyRandom(new EasyRandomParameters()
                .randomize(TradingNote.class, new TradingNoteRandomizer()));

        assertFalse(validator.isValid(generator.nextObject(TradingNote.class), validatorContext));
    }
}