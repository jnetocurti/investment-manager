package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;

import java.util.StringJoiner;

public final class PortfolioEventIdempotencyKeyFactory {

    private PortfolioEventIdempotencyKeyFactory() {
    }

    public static String generate(PortfolioEvent event) {
        StringJoiner joiner = new StringJoiner("|");
        joiner.add(value(event.getEventType() != null ? event.getEventType().name() : null));
        joiner.add(value(event.getEventSource() != null ? event.getEventSource().name() : null));
        joiner.add(value(event.getAssetName()));
        joiner.add(value(event.getAssetType() != null ? event.getAssetType().name() : null));
        joiner.add(value(event.getBrokerId()));
        joiner.add(value(event.getEventDate() != null ? event.getEventDate().toString() : null));
        joiner.add(value(event.getSourceReferenceId()));
        joiner.add(value(event.getMetadata() != null ? event.getMetadata().getSubscriptionTicker() : null));
        return joiner.toString();
    }

    private static String value(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
