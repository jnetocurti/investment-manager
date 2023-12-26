package br.com.investmentmanager.shared.domain;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import lombok.NonNull;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot extends Entity {

    private transient final @Transient List<DomainEvent> domainEvents = new ArrayList<>();

    public AggregateRoot() {
        super();
    }

    protected void registerEvent(@NonNull DomainEvent event) {
        this.domainEvents.add(event);
    }

    public Collection<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
