package br.com.investmentmanager.portfolioevent.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PortfolioEventService {

    private final PortfolioEventRepository repository;

    private final ApplicationEventPublisher publisher;

    public void create(List<PortfolioEvent> portfolioEvents) {
        repository.save(portfolioEvents);
        portfolioEvents.stream().flatMap(p -> p.domainEvents().parallelStream()).forEach(publisher::publishEvent);
    }
}
