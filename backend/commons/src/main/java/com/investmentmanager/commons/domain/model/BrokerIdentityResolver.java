package com.investmentmanager.commons.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrokerIdentityResolver {

    private static final List<BrokerAliasGroup> ALIAS_GROUPS = List.of(
            new BrokerAliasGroup(
                    "CLEAR",
                    Set.of("CLEAR CORRETORA", "CLEAR"),
                    Set.of("02.332.886/0001-04", "45.246.575/0001-78")),
            new BrokerAliasGroup(
                    "NUINVEST",
                    Set.of("EASYINVEST", "NUINVEST", "NU INVEST"),
                    Set.of("62.169.875/0001-79"))
    );

    public static BrokerIdentity resolve(String brokerName, String brokerDocument) {
        String normalizedName = normalizeName(brokerName);
        String normalizedDocument = normalizeDocument(brokerDocument);

        for (BrokerAliasGroup group : ALIAS_GROUPS) {
            if (group.matches(normalizedName, normalizedDocument)) {
                LinkedHashSet<String> knownDocuments = new LinkedHashSet<>(group.getDocuments());
                if (brokerDocument != null && !brokerDocument.isBlank()) {
                    knownDocuments.add(brokerDocument);
                }
                return new BrokerIdentity(group.getCanonicalKey(), knownDocuments);
            }
        }

        String fallbackKey = !normalizedDocument.isBlank()
                ? normalizedDocument
                : (!normalizedName.isBlank() ? normalizedName : "UNKNOWN_BROKER");

        LinkedHashSet<String> fallbackDocuments = new LinkedHashSet<>();
        if (brokerDocument != null && !brokerDocument.isBlank()) {
            fallbackDocuments.add(brokerDocument);
        }

        return new BrokerIdentity(fallbackKey, fallbackDocuments);
    }

    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    private static String normalizeDocument(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "").trim();
    }

    @Getter
    @AllArgsConstructor
    public static class BrokerIdentity {

        private final String brokerKey;
        private final Set<String> knownDocuments;
    }

    @Getter
    private static class BrokerAliasGroup {

        private final String canonicalKey;
        private final Set<String> names;
        private final Set<String> documents;
        private final Set<String> normalizedNames;
        private final Set<String> normalizedDocuments;

        private BrokerAliasGroup(String canonicalKey, Set<String> names, Set<String> documents) {
            this.canonicalKey = canonicalKey;
            this.names = names;
            this.documents = documents;
            this.normalizedNames = names.stream().map(BrokerIdentityResolver::normalizeName).collect(java.util.stream.Collectors.toSet());
            this.normalizedDocuments = documents.stream().map(BrokerIdentityResolver::normalizeDocument).collect(java.util.stream.Collectors.toSet());
        }

        private boolean matches(String normalizedName, String normalizedDocument) {
            return normalizedNames.contains(normalizedName) || normalizedDocuments.contains(normalizedDocument);
        }
    }
}
