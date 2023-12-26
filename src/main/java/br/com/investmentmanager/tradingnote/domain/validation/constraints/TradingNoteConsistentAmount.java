package br.com.investmentmanager.tradingnote.domain.validation.constraints;

import br.com.investmentmanager.tradingnote.domain.validation.TradingNoteConsistentAmountValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {TradingNoteConsistentAmountValidator.class})
public @interface TradingNoteConsistentAmount {
    String message() default "{br.com.investmentmanager.constraints.TradingNoteConsistentAmountValidator.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
