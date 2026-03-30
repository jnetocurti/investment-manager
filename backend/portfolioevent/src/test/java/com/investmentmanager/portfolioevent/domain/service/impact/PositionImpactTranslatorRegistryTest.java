package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PositionImpactTranslatorRegistryTest {

    private final PositionImpactTranslatorRegistry registry = PositionImpactTranslatorRegistry.defaultRegistry();

    @Test
    void shouldTranslateBuyToSingleIncreaseImpact() {
        PortfolioEvent event = PortfolioEvent.builder()
                .id("evt-1")
                .eventType(EventType.BUY)
                .eventSource(EventSource.TRADING_NOTE)
                .assetName("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(100)
                .unitPrice(MonetaryValue.of("10"))
                .totalValue(MonetaryValue.of("1000"))
                .fee(MonetaryValue.of("2"))
                .currency("BRL")
                .eventDate(LocalDate.of(2024, 1, 10))
                .brokerId("broker-1")
                .sourceReferenceId("note-1")
                .idempotencyKey("k1")
                .createdAt(LocalDateTime.now())
                .build();

        List<PositionImpactEvent> impacts = registry.translate(event);

        assertEquals(1, impacts.size());
        assertEquals("PETR4", impacts.getFirst().getTicker());
    }

    @Test
    void shouldReturnEmptyForSubscriptionPending() {
        PortfolioEvent event = PortfolioEvent.builder()
                .id("evt-sub")
                .eventType(EventType.SUBSCRIPTION)
                .eventSource(EventSource.SUBSCRIPTION)
                .assetName("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(100)
                .unitPrice(MonetaryValue.of("10"))
                .totalValue(MonetaryValue.of("1000"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2024, 2, 1))
                .brokerId("broker-1")
                .sourceReferenceId("s")
                .idempotencyKey("k2")
                .metadata(PortfolioEventMetadata.subscription("PETR12"))
                .createdAt(LocalDateTime.now())
                .build();

        assertTrue(registry.translate(event).isEmpty());
    }
}
