package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventIdempotencyKey;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioEventServiceIdempotencyTest {

    @Test
    void shouldGenerateSameIdempotencyKeyForSameInput() {
        String key1 = PortfolioEventIdempotencyKey.of(
                EventType.BUY,
                "PETR4",
                AssetType.STOCKS_BRL,
                LocalDate.of(2026, 3, 31),
                "BROKER_CLEAR",
                "note-001").value();

        String key2 = PortfolioEventIdempotencyKey.of(
                EventType.BUY,
                "PETR4",
                AssetType.STOCKS_BRL,
                LocalDate.of(2026, 3, 31),
                "BROKER_CLEAR",
                "note-001").value();

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void shouldGenerateDifferentIdempotencyKeyWhenSourceReferenceChanges() {
        String key1 = PortfolioEventIdempotencyKey.of(
                EventType.BUY,
                "PETR4",
                AssetType.STOCKS_BRL,
                LocalDate.of(2026, 3, 31),
                "BROKER_CLEAR",
                "note-001").value();

        String key2 = PortfolioEventIdempotencyKey.of(
                EventType.BUY,
                "PETR4",
                AssetType.STOCKS_BRL,
                LocalDate.of(2026, 3, 31),
                "BROKER_CLEAR",
                "note-002").value();

        assertThat(key1).isNotEqualTo(key2);
    }
    @Test
    void shouldNormalizeSemanticInputIntoSameIdempotencyKey() {
        String key1 = PortfolioEventIdempotencyKey.of(
                EventType.BUY,
                "petr4",
                AssetType.STOCKS_BRL,
                LocalDate.of(2026, 3, 31),
                "broker_clear",
                " note-001 ").value();

        String key2 = PortfolioEventIdempotencyKey.of(
                EventType.BUY,
                "PETR4",
                AssetType.STOCKS_BRL,
                LocalDate.of(2026, 3, 31),
                "BROKER_CLEAR",
                "NOTE-001").value();

        assertThat(key1).isEqualTo(key2);
    }

}
