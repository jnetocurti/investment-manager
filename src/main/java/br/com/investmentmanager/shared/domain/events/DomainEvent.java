package br.com.investmentmanager.shared.domain.events;

import lombok.NonNull;
import org.springframework.context.ApplicationEvent;

public class DomainEvent<T> extends ApplicationEvent {

    protected DomainEvent(@NonNull T source) {
        super(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getSource() {
        return (T) super.getSource();
    }
}
