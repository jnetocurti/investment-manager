package br.com.investmentmanager.shared.domain.aggregate;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import lombok.NonNull;

import java.util.List;

public abstract class AbstractAggregateRoot<T extends org.springframework.data.domain.AbstractAggregateRoot<T>>
        extends org.springframework.data.domain.AbstractAggregateRoot<T> {

    @Override
    protected <E> E registerEvent(@NonNull E event) {
        if (!(event instanceof DomainEvent<?>)) {
            throw new IllegalArgumentException("The event parameter must be of type DomainEvent");
        }

        return super.registerEvent(event);
    }

    @SuppressWarnings("rawtypes")
    public List<DomainEvent> getDomainEvents() {
        return super.domainEvents().stream().map(e -> (DomainEvent) e).toList();
    }
}
