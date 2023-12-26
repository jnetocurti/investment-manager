package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static br.com.investmentmanager.shared.util.constants.Currency.BRL;
import static org.junit.jupiter.api.Assertions.*;

class TradingNoteTest {

    private final EasyRandom generator = new EasyRandom(new EasyRandomParameters()
            .randomize(MonetaryValue.class, () -> MonetaryValue.of(BRL, BigDecimal.ONE))
            .randomize(BigDecimal.class, () -> BigDecimal.ONE));

    private static String getValidationAssertion(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + " -> " + violation.getMessageTemplate();
    }

    @Test
    void validateSuccess() {
        var tradingNote = TradingNote.builder()
                .broker(generator.nextObject(String.class))
                .invoiceNumber(generator.nextObject(String.class))
                .netAmount(generator.nextObject(MonetaryValue.class))
                .costs(generator.nextObject(MonetaryValue.class))
                .tradingDate(generator.nextObject(LocalDate.class))
                .liquidateDate(generator.nextObject(LocalDate.class))
                .items(generator.objects(TradingNoteItem.class, 1).toList())
                .build();

        assertNotNull(tradingNote);
    }

    @Test
    void validateFailureAtRoot() {
        var exception = assertThrows(ConstraintViolationException.class, () -> TradingNote.builder().build());

        assertEquals(8, exception.getConstraintViolations().size());
        assertTrue(exception.getConstraintViolations().stream().map(TradingNoteTest::getValidationAssertion).toList()
                .containsAll(List.of(
                        "invoiceNumber -> {jakarta.validation.constraints.NotBlank.message}",
                        "costs -> {jakarta.validation.constraints.NotNull.message}",
                        "broker -> {jakarta.validation.constraints.NotBlank.message}",
                        "items -> {jakarta.validation.constraints.NotEmpty.message}",
                        "liquidateDate -> {jakarta.validation.constraints.NotNull.message}",
                        "tradingDate -> {jakarta.validation.constraints.NotNull.message}",
                        "netAmount -> {jakarta.validation.constraints.NotNull.message}",
                        " -> {br.com.investmentmanager.constraints.TradingNoteConsistentAmountValidator.message}"
                )));
    }

    @Test
    void validateFailureOnItems() {
        var exception = assertThrows(ConstraintViolationException.class, () -> TradingNote.builder()
                .broker(generator.nextObject(String.class))
                .invoiceNumber(generator.nextObject(String.class))
                .netAmount(generator.nextObject(MonetaryValue.class))
                .costs(generator.nextObject(MonetaryValue.class))
                .tradingDate(generator.nextObject(LocalDate.class))
                .liquidateDate(generator.nextObject(LocalDate.class))
                .items(List.of(TradingNoteItem.builder().build()))
                .build());

        assertEquals(5, exception.getConstraintViolations().size());
        assertTrue(exception.getConstraintViolations().stream().map(TradingNoteTest::getValidationAssertion).toList()
                .containsAll(List.of(
                        "operation -> {jakarta.validation.constraints.NotNull.message}",
                        "assetCode -> {jakarta.validation.constraints.NotBlank.message}",
                        "quantity -> {jakarta.validation.constraints.NotNull.message}",
                        "costs -> {jakarta.validation.constraints.NotNull.message}",
                        "unitPrice -> {jakarta.validation.constraints.NotNull.message}"
                )));
    }

    @Test
    void validateFailureOnAmount() {
        var exception = assertThrows(ConstraintViolationException.class, () -> TradingNote.builder()
                .broker(generator.nextObject(String.class))
                .invoiceNumber(generator.nextObject(String.class))
                .netAmount(generator.nextObject(MonetaryValue.class))
                .costs(generator.nextObject(MonetaryValue.class))
                .tradingDate(generator.nextObject(LocalDate.class))
                .liquidateDate(generator.nextObject(LocalDate.class))
                .items(generator.objects(TradingNoteItem.class, 5).toList())
                .build());

        assertEquals(1, exception.getConstraintViolations().size());
        assertTrue(exception.getConstraintViolations().stream().map(TradingNoteTest::getValidationAssertion).toList()
                .contains(" -> {br.com.investmentmanager.constraints.TradingNoteConsistentAmountValidator.message}"));
    }

    @Test
    void getTotalAmount() {
        var tradingNote = TradingNote.builder()
                .broker(generator.nextObject(String.class))
                .invoiceNumber(generator.nextObject(String.class))
                .netAmount(generator.nextObject(MonetaryValue.class))
                .costs(generator.nextObject(MonetaryValue.class))
                .tradingDate(generator.nextObject(LocalDate.class))
                .liquidateDate(generator.nextObject(LocalDate.class))
                .items(generator.objects(TradingNoteItem.class, 1).toList())
                .build();

        assertEquals(BigDecimal.valueOf(2), tradingNote.getTotalAmount().getValue());
    }
}