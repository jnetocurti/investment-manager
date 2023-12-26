package br.com.investmentmanager.shared.domain;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Entity {

    private static final Validator VALIDATOR;

    static {
        VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @NotNull
    protected final UUID id;

    protected Entity() {
        id = UUID.randomUUID();
    }

    protected final void validate() {
        var violations = VALIDATOR.validate(this);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}