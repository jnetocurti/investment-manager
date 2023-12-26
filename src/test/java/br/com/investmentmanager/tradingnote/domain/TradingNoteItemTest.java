package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static br.com.investmentmanager.shared.util.constants.Currency.BRL;
import static org.junit.jupiter.api.Assertions.*;

class TradingNoteItemTest {

    private final EasyRandom generator = new EasyRandom(new EasyRandomParameters()
            .randomize(MonetaryValue.class, () -> MonetaryValue.of(BRL, BigDecimal.ONE))
            .randomize(BigDecimal.class, () -> BigDecimal.ONE));

    private static String getValidationAssertion(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + " -> " + violation.getMessageTemplate();
    }

    @Test
    void validateSuccess() {
        var tradingNoteItem = TradingNoteItem.builder()
                .assetCode(generator.nextObject(String.class))
                .quantity(generator.nextObject(BigDecimal.class))
                .unitPrice(generator.nextObject(MonetaryValue.class))
                .costs(generator.nextObject(MonetaryValue.class))
                .operation(generator.nextObject(Operation.class))
                .build();

        assertNotNull(tradingNoteItem);
    }

    @Test
    void validateFailure() {
        var exception = assertThrows(ConstraintViolationException.class, () -> TradingNoteItem.builder().build());

        assertEquals(5, exception.getConstraintViolations().size());
        assertTrue(exception.getConstraintViolations().stream()
                .map(TradingNoteItemTest::getValidationAssertion).toList()
                .containsAll(List.of(
                        "quantity -> {jakarta.validation.constraints.NotNull.message}",
                        "costs -> {jakarta.validation.constraints.NotNull.message}",
                        "operation -> {jakarta.validation.constraints.NotNull.message}",
                        "assetCode -> {jakarta.validation.constraints.NotBlank.message}",
                        "unitPrice -> {jakarta.validation.constraints.NotNull.message}"
                )));
    }

    @Test
    void getNetAmount() {
        var tradingNoteItem = TradingNoteItem.builder()
                .assetCode(generator.nextObject(String.class))
                .quantity(generator.nextObject(BigDecimal.class))
                .unitPrice(generator.nextObject(MonetaryValue.class))
                .costs(generator.nextObject(MonetaryValue.class))
                .operation(generator.nextObject(Operation.class))
                .build();

        assertEquals(BigDecimal.valueOf(1), tradingNoteItem.getNetAmount().getValue());
    }

    @Test
    void getTotalAmount() {
        var tradingNoteItem = TradingNoteItem.builder()
                .assetCode(generator.nextObject(String.class))
                .quantity(generator.nextObject(BigDecimal.class))
                .unitPrice(generator.nextObject(MonetaryValue.class))
                .costs(generator.nextObject(MonetaryValue.class))
                .operation(generator.nextObject(Operation.class))
                .build();

        assertEquals(BigDecimal.valueOf(2), tradingNoteItem.getTotalAmount().getValue());
    }
}