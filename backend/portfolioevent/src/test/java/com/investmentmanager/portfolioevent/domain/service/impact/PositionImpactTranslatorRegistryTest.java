package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionImpactTranslatorRegistryTest {

    @Test
    void shouldTranslateBuyIntoImpactWithBrokerKey() {
        PositionImpactTranslatorRegistry registry = new PositionImpactTranslatorRegistry(
                List.of(new BuyEventImpactTranslator(), new SellEventImpactTranslator(),
                        new SubscriptionPendingImpactTranslator(), new SubscriptionConversionImpactTranslator()));

        PortfolioEvent buy = PortfolioEvent.builder()
                .id("event-1")
                .eventType(EventType.BUY)
                .eventSource(EventSource.TRADING_NOTE)
                .assetName("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(10)
                .unitPrice(MonetaryValue.of("30"))
                .totalValue(MonetaryValue.of("300"))
                .fee(MonetaryValue.of("1"))
                .currency("BRL")
                .eventDate(LocalDate.of(2024, 1, 10))
                .brokerKey("BROKER_XP")
                .sourceReferenceId("note-1")
                .idempotencyKey("idemp")
                .createdAt(LocalDateTime.now())
                .build();

        var impacts = registry.translate(buy);

        assertThat(impacts).hasSize(1);
        assertThat(impacts.getFirst().getBrokerKey()).isEqualTo("BROKER_XP");
    }

    @Test
    void shouldFailWhenFactorIsInvalid() {
        PositionImpactEvent impact = PositionImpactEvent.builder()
                .originalEventId("x")
                .ticker("ITSA4")
                .impactType(com.investmentmanager.commons.domain.model.PositionImpactType.ADJUST)
                .sequence(1)
                .quantity(10)
                .unitPrice(MonetaryValue.of("10"))
                .fee(MonetaryValue.zero())
                .factor(BigDecimal.ZERO)
                .eventDate(LocalDate.of(2024, 1, 1))
                .originType(EventType.BUY)
                .sourceType(com.investmentmanager.portfolioevent.domain.model.ImpactSourceType.CORPORATE_ACTION)
                .brokerKey("BROKER_XP")
                .sourceReferenceId("s")
                .schemaVersion(1)
                .createdAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(impact::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Factor must be > 0");
    }
}
