package br.com.investmentmanager.shared.domain.exceptions;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

public class EntityAlreadyExistsException extends RuntimeException {

    @Getter
    private final UUID entityId;

    public EntityAlreadyExistsException(@NonNull String message, @NonNull UUID entityId) {
        super(message);
        this.entityId = entityId;
    }
}
