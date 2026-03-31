package com.investmentmanager.portfolioevent.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;

public record PortfolioEventIdempotencyKey(String value) {

    public static PortfolioEventIdempotencyKey of(EventType eventType,
                                                  String asset,
                                                  AssetType assetType,
                                                  LocalDate eventDate,
                                                  String brokerKey,
                                                  String sourceReferenceId) {
        String canonical = String.join("|",
                normalize(eventType != null ? eventType.name() : null),
                normalize(asset),
                normalize(assetType != null ? assetType.name() : null),
                normalize(eventDate != null ? eventDate.toString() : null),
                normalize(brokerKey),
                normalize(sourceReferenceId));
        return new PortfolioEventIdempotencyKey(hash(canonical));
    }

    private static String normalize(String input) {
        return input == null ? "" : input.trim().toUpperCase(Locale.ROOT);
    }

    private static String hash(String canonical) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
