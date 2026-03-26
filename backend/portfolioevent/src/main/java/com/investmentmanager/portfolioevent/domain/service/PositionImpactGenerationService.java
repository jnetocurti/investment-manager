package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventRepositoryPort;
import com.investmentmanager.portfolioevent.domain.service.impact.PositionImpactTranslatorRegistry;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PositionImpactGenerationService {

    private final PositionImpactTranslatorRegistry impactTranslatorRegistry;
    private final PositionImpactEventRepositoryPort impactRepository;
    private final PositionImpactEventPublisherPort impactPublisher;

    public List<PositionImpactEvent> generateAndPublish(List<PortfolioEvent> events) {
        List<PositionImpactEvent> impacts = events.stream()
                .flatMap(event -> impactTranslatorRegistry.translate(event).stream())
                .peek(PositionImpactEvent::validate)
                .filter(impact -> !impactRepository.existsByUniqueKey(
                        impact.getOriginalEventId(),
                        impact.getImpactType().name(),
                        impact.getSequence()))
                .toList();

        List<PositionImpactEvent> persisted = impactRepository.saveAll(impacts);
        impactPublisher.publishAll(persisted);
        return persisted;
    }
}
