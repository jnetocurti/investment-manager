package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioEventDocumentMapperTest {

    @Test
    void shouldMapMetadataAndBrokerIdToDocumentAndBack() {
        PortfolioEvent event = PortfolioEvent.builder()
                .id("evt-1")
                .eventType(EventType.SUBSCRIPTION)
                .eventSource(EventSource.SUBSCRIPTION)
                .assetName("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(10)
                .unitPrice(MonetaryValue.of("10"))
                .totalValue(MonetaryValue.of("100"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 3, 30))
                .brokerId("broker-1")
                .sourceReferenceId("SUBSCRIPTION:PETR4:STOCKS_BRL:broker-1:2026-03-30")
                .idempotencyKey("IDEMP-1")
                .metadata(PortfolioEventMetadata.subscription("PETR12"))
                .createdAt(LocalDateTime.now())
                .build();

        PortfolioEventDocument doc = PortfolioEventDocumentMapper.toDocument(event);

        assertEquals("broker-1", doc.getBrokerId());
        assertEquals("IDEMP-1", doc.getIdempotencyKey());
        assertNotNull(doc.getMetadata());
        assertEquals("PETR12", doc.getMetadata().getSubscriptionTicker());

        PortfolioEvent mapped = PortfolioEventDocumentMapper.toDomain(doc);

        assertEquals("broker-1", mapped.getBrokerId());
        assertEquals("IDEMP-1", mapped.getIdempotencyKey());
        assertNotNull(mapped.getMetadata());
        assertEquals("PETR12", mapped.getMetadata().getSubscriptionTicker());
    }
}
