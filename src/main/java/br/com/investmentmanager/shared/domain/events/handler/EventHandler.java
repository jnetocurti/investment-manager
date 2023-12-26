package br.com.investmentmanager.shared.domain.events.handler;

import br.com.investmentmanager.shared.domain.events.DomainEvent;
import lombok.NonNull;
import org.springframework.context.event.EventListener;

public interface EventHandler<E extends DomainEvent> {

    @EventListener
    void handle(@NonNull E event);
}
