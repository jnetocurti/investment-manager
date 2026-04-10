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

    @Test
    void shouldMapTickerRenameMetadataToDocumentAndBack() {
        PortfolioEvent event = PortfolioEvent.builder()
                .id("evt-rename-1")
                .eventType(EventType.TICKER_RENAME)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("GGRC11")
                .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                .quantity(25)
                .unitPrice(MonetaryValue.of("100"))
                .totalValue(MonetaryValue.of("2500"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 4, 1))
                .brokerKey("BROKER_CLEAR")
                .idempotencyKey("idempotency-rename-1")
                .sourceReferenceId("TICKER_RENAME:GETT11:GGRC11:2026-04-01")
                .metadata(PortfolioEventMetadata.tickerRename("GETT11", "GGRC11"))
                .createdAt(LocalDateTime.now())
                .build();

        PortfolioEventDocument doc = PortfolioEventDocumentMapper.toDocument(event);
        assertNotNull(doc.getMetadata());
        assertEquals("GETT11", doc.getMetadata().getOldTicker());
        assertEquals("GGRC11", doc.getMetadata().getNewTicker());

        PortfolioEvent mapped = PortfolioEventDocumentMapper.toDomain(doc);
        assertNotNull(mapped.getMetadata());
        assertEquals("GETT11", mapped.getMetadata().getOldTicker());
        assertEquals("GGRC11", mapped.getMetadata().getNewTicker());
    }

    @Test
    void shouldMapBonusMetadataToDocumentAndBack() {
        PortfolioEvent event = PortfolioEvent.builder()
                .id("evt-bonus-1")
                .eventType(EventType.BONUS)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("BBDC3")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(5)
                .unitPrice(MonetaryValue.of("4.527177676"))
                .totalValue(MonetaryValue.of("22.635888380"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2021, 4, 23))
                .brokerKey("BROKER_CLEAR")
                .idempotencyKey("idempotency-bonus-1")
                .sourceReferenceId("BONUS:BBDC3:2021-04-23:1:10")
                .metadata(PortfolioEventMetadata.bonus("1:10", 50))
                .createdAt(LocalDateTime.now())
                .build();

        PortfolioEventDocument doc = PortfolioEventDocumentMapper.toDocument(event);
        assertNotNull(doc.getMetadata());
        assertEquals("1:10", doc.getMetadata().getBonusRatio());
        assertEquals(50, doc.getMetadata().getBonusBaseQuantity());

        PortfolioEvent mapped = PortfolioEventDocumentMapper.toDomain(doc);
        assertNotNull(mapped.getMetadata());
        assertEquals("1:10", mapped.getMetadata().getBonusRatio());
        assertEquals(50, mapped.getMetadata().getBonusBaseQuantity());
    }
}
