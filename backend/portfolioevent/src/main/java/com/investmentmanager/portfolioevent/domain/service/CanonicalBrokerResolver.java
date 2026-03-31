package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerCatalogRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class CanonicalBrokerResolver {

    private static final List<BrokerAliasRule> ALIAS_RULES = List.of(
            new BrokerAliasRule("BROKER_CLEAR", Set.of("CLEAR", "CLEAR CORRETORA"), Set.of("02332886000104", "45246575000178")),
            new BrokerAliasRule("BROKER_NUINVEST", Set.of("NUINVEST", "NU INVEST", "EASYINVEST"), Set.of("62169875000179"))
    );

    private final BrokerCatalogRepositoryPort repository;

    public CanonicalBroker findOrCreateCanonicalBroker(BrokerResolutionInput input) {
        String normalizedName = normalizeName(input.getName());
        String normalizedDocument = normalizeDocument(input.getDocument());

        Optional<BrokerAliasRule> matchingRule = ALIAS_RULES.stream()
                .filter(rule -> rule.matches(normalizedName, normalizedDocument))
                .findFirst();

        Optional<CanonicalBroker> existing = matchingRule
                .flatMap(rule -> repository.findByBrokerKey(rule.brokerKey()))
                .or(() -> !normalizedDocument.isBlank() ? repository.findByNormalizedDocument(normalizedDocument) : Optional.empty())
                .or(() -> !normalizedName.isBlank() ? repository.findByNormalizedName(normalizedName) : Optional.empty());

        if (existing.isPresent()) {
            return repository.save(merge(existing.get(), input));
        }

        String brokerKey = matchingRule
                .map(BrokerAliasRule::brokerKey)
                .orElseGet(() -> buildBrokerKey(normalizedName, normalizedDocument));

        LocalDateTime now = LocalDateTime.now();
        LinkedHashSet<String> names = new LinkedHashSet<>();
        LinkedHashSet<String> documents = new LinkedHashSet<>();
        if (input.getName() != null && !input.getName().isBlank()) {
            names.add(input.getName());
        }
        if (input.getDocument() != null && !input.getDocument().isBlank()) {
            documents.add(input.getDocument());
        }
        return repository.save(CanonicalBroker.builder()
                .brokerKey(brokerKey)
                .currentName(input.getName())
                .currentDocument(input.getDocument())
                .knownNames(names)
                .knownDocuments(documents)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private CanonicalBroker merge(CanonicalBroker existing, BrokerResolutionInput input) {
        LinkedHashSet<String> names = new LinkedHashSet<>(existing.getKnownNames());
        LinkedHashSet<String> docs = new LinkedHashSet<>(existing.getKnownDocuments());
        if (input.getName() != null && !input.getName().isBlank()) {
            names.add(input.getName());
        }
        if (input.getDocument() != null && !input.getDocument().isBlank()) {
            docs.add(input.getDocument());
        }

        return existing.toBuilder()
                .currentName(input.getName() != null ? input.getName() : existing.getCurrentName())
                .currentDocument(input.getDocument() != null ? input.getDocument() : existing.getCurrentDocument())
                .knownNames(names)
                .knownDocuments(docs)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private String buildBrokerKey(String normalizedName, String normalizedDocument) {
        if (!normalizedName.isBlank()) {
            return "BROKER_" + normalizedName;
        }
        if (!normalizedDocument.isBlank()) {
            return "BROKER_CNPJ_" + normalizedDocument;
        }
        return "BROKER_UNKNOWN";
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    private String normalizeDocument(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "").trim();
    }

    private record BrokerAliasRule(String brokerKey, Set<String> names, Set<String> documents) {
        boolean matches(String normalizedName, String normalizedDocument) {
            return names.stream().map(this::normalizeName).anyMatch(normalizedName::equals)
                    || documents.stream().map(this::normalizeDocument).anyMatch(normalizedDocument::equals);
        }

        private String normalizeName(String value) {
            return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "").trim();
        }

        private String normalizeDocument(String value) {
            return value.replaceAll("[^0-9]", "").trim();
        }
    }
}
