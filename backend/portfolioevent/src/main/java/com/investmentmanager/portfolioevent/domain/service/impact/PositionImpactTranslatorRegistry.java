package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PositionImpactTranslatorRegistry {

    private final List<PortfolioEventImpactTranslator> translators;

    public List<PositionImpactEvent> translate(PortfolioEvent event) {
        return translators.stream()
                .filter(t -> t.supports(event))
                .findFirst()
                .map(t -> t.translate(event))
                .orElseThrow(() -> new IllegalStateException(
                        "No impact translator for event type " + event.getEventType()));
    }
}
