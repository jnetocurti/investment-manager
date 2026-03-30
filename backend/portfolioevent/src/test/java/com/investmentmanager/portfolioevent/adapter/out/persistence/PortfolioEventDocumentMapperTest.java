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
    void shouldMapMetadataAndBrokerKeyToDocumentAndBack() {
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
                .brokerName("Clear")
                .brokerDocument("02.332.886/0001-04")
                .brokerKey("CLEAR")
                .sourceReferenceId("SUBSCRIPTION:PETR4:STOCKS_BRL:CLEAR:2026-03-30")
                .metadata(PortfolioEventMetadata.subscription("PETR12"))
                .createdAt(LocalDateTime.now())
                .build();

        PortfolioEventDocument doc = PortfolioEventDocumentMapper.toDocument(event);

        assertEquals("CLEAR", doc.getBrokerKey());
        assertNotNull(doc.getMetadata());
        assertEquals("PETR12", doc.getMetadata().getSubscriptionTicker());

        PortfolioEvent mapped = PortfolioEventDocumentMapper.toDomain(doc);

        assertEquals("CLEAR", mapped.getBrokerKey());
        assertNotNull(mapped.getMetadata());
        assertEquals("PETR12", mapped.getMetadata().getSubscriptionTicker());
    }
}
