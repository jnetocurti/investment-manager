package com.investmentmanager.portfolioevent.domain.exception;

public class IdempotentOperationException extends IllegalStateException {

    public IdempotentOperationException(String message) {
        super(message);
    }
}
