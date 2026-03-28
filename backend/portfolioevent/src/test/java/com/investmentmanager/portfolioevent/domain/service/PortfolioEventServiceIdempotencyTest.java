package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventRepositoryPort;
import com.investmentmanager.portfolioevent.domain.service.impact.PortfolioEventImpactTranslator;
import com.investmentmanager.portfolioevent.domain.service.impact.PositionImpactTranslatorRegistry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PortfolioEventServiceIdempotencyTest {

    @Test
    void shouldHandleMultipleImpactsFromSameEventWithoutDuplication() {
        PositionImpactEventRepositoryPort impactRepository = mock(PositionImpactEventRepositoryPort.class);
        PositionImpactEventPublisherPort impactPublisher = mock(PositionImpactEventPublisherPort.class);

        PortfolioEventImpactTranslator multiImpactTranslator = new PortfolioEventImpactTranslator() {
            @Override
            public boolean supports(PortfolioEvent event) {
                return true;
            }

            @Override
            public List<PositionImpactEvent> translate(PortfolioEvent event) {
                return List.of(
                        impact(event, 1),
                        impact(event, 2)
                );
            }

            private PositionImpactEvent impact(PortfolioEvent event, int sequence) {
                return PositionImpactEvent.builder()
                        .originalEventId(event.getId())
                        .ticker(event.getAssetName())
                        .impactType(PositionImpactType.ADJUST)
                        .sequence(sequence)
                        .quantity(100)
                        .unitPrice(MonetaryValue.of("10"))
                        .fee(MonetaryValue.zero())
                        .eventDate(event.getEventDate())
                        .originType(event.getEventType())
                        .sourceType(ImpactSourceType.CORPORATE_ACTION)
                        .brokerName(event.getBrokerName())
                        .brokerDocument(event.getBrokerDocument())
                        .sourceReferenceId(event.getSourceReferenceId())
                        .schemaVersion(1)
                        .createdAt(LocalDateTime.now())
                        .build();
            }
        };

        PositionImpactTranslatorRegistry registry = new PositionImpactTranslatorRegistry(List.of(multiImpactTranslator));
        PositionImpactGenerationService service = new PositionImpactGenerationService(registry, impactRepository, impactPublisher);

        PortfolioEvent event = PortfolioEvent.builder()
                .id("evt-1")
                .eventType(EventType.BUY)
                .eventSource(EventSource.TRADING_NOTE)
                .assetName("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(10)
                .unitPrice(MonetaryValue.of("10"))
                .totalValue(MonetaryValue.of("100"))
                .fee(MonetaryValue.of("1"))
                .currency("BRL")
                .eventDate(LocalDate.of(2024, 1, 1))
                .brokerName("XP")
                .brokerDocument("123")
                .sourceReferenceId("note-1")
                .createdAt(LocalDateTime.now())
                .build();

        when(impactRepository.existsByUniqueKey("evt-1", "ADJUST", 1)).thenReturn(true);
        when(impactRepository.existsByUniqueKey("evt-1", "ADJUST", 2)).thenReturn(false);
        when(impactRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        service.generateAndPublish(List.of(event));

        verify(impactRepository, times(1)).saveAll(any());
        verify(impactPublisher, times(1)).publishAll(any());
    }
}
