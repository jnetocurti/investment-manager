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
    void shouldMapMetadataBrokerKeyAndIdempotencyToDocumentAndBack() {
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
                .brokerKey("BROKER_CLEAR")
                .idempotencyKey("idempotency-1")
                .sourceReferenceId("SUBSCRIPTION:PETR4:2026-03-30")
                .metadata(PortfolioEventMetadata.subscription("PETR12"))
                .createdAt(LocalDateTime.now())
                .build();

        PortfolioEventDocument doc = PortfolioEventDocumentMapper.toDocument(event);

        assertEquals("BROKER_CLEAR", doc.getBrokerKey());
        assertEquals("idempotency-1", doc.getIdempotencyKey());
        assertNotNull(doc.getMetadata());

        PortfolioEvent mapped = PortfolioEventDocumentMapper.toDomain(doc);

        assertEquals("BROKER_CLEAR", mapped.getBrokerKey());
        assertEquals("idempotency-1", mapped.getIdempotencyKey());
        assertEquals("PETR12", mapped.getMetadata().getSubscriptionTicker());
    }

    @Test
    void shouldMapSplitFractionMetadataToDocumentAndBack() {
        PortfolioEvent event = PortfolioEvent.builder()
                .id("evt-split-1")
                .eventType(EventType.SPLIT)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("ITSA4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(1)
                .unitPrice(MonetaryValue.zero())
                .totalValue(MonetaryValue.zero())
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 3, 30))
                .brokerKey("BROKER_CLEAR")
                .idempotencyKey("idempotency-split-1")
                .sourceReferenceId("SPLIT:ITSA4:2026-03-30:1:2")
                .metadata(PortfolioEventMetadata.builder()
                        .splitRatio("1:2")
                        .splitFractionResidualBookValue(new java.math.BigDecimal("3.33"))
                        .splitFractionFlowStatus("PENDING_SETTLEMENT")
                        .splitFractionSourceReferenceId("SPLIT:ITSA4:2026-03-30:1:2")
                        .build())
                .createdAt(LocalDateTime.now())
                .build();

        PortfolioEventDocument doc = PortfolioEventDocumentMapper.toDocument(event);
        assertNotNull(doc.getMetadata());
        assertEquals("3.33", doc.getMetadata().getSplitFractionResidualBookValue().toPlainString());
        assertEquals("PENDING_SETTLEMENT", doc.getMetadata().getSplitFractionFlowStatus());
        assertEquals("SPLIT:ITSA4:2026-03-30:1:2", doc.getMetadata().getSplitFractionSourceReferenceId());

        PortfolioEvent mapped = PortfolioEventDocumentMapper.toDomain(doc);
        assertNotNull(mapped.getMetadata());
        assertEquals("3.33", mapped.getMetadata().getSplitFractionResidualBookValue().toPlainString());
        assertEquals("PENDING_SETTLEMENT", mapped.getMetadata().getSplitFractionFlowStatus());
        assertEquals("SPLIT:ITSA4:2026-03-30:1:2", mapped.getMetadata().getSplitFractionSourceReferenceId());
    }
}
