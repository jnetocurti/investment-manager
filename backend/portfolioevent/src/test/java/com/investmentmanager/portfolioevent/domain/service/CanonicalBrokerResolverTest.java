package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerCatalogRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CanonicalBrokerResolverTest {

    private BrokerCatalogRepositoryPort repository;
    private CanonicalBrokerResolver resolver;

    @BeforeEach
    void setup() {
        repository = mock(BrokerCatalogRepositoryPort.class);
        resolver = new CanonicalBrokerResolver(repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldResolveDifferentInputsToSameCanonicalBrokerKey() {
        CanonicalBroker existing = CanonicalBroker.builder()
                .brokerKey("BROKER_CLEAR")
                .currentName("CLEAR")
                .currentDocument("02.332.886/0001-04")
                .knownNames(Set.of("CLEAR"))
                .knownDocuments(Set.of("02.332.886/0001-04"))
                .build();
        when(repository.findByBrokerKey("BROKER_CLEAR")).thenReturn(Optional.of(existing));

        CanonicalBroker resolved = resolver.findOrCreateCanonicalBroker(BrokerResolutionInput.builder()
                .name("Clear Corretora")
                .document("45.246.575/0001-78")
                .build());

        assertThat(resolved.getBrokerKey()).isEqualTo("BROKER_CLEAR");
    }

    @Test
    void shouldCreateNewCanonicalBrokerWhenNoMatchExists() {
        when(repository.findByBrokerKey(any())).thenReturn(Optional.empty());
        when(repository.findByNormalizedDocument(any())).thenReturn(Optional.empty());
        when(repository.findByNormalizedName(any())).thenReturn(Optional.empty());

        CanonicalBroker created = resolver.findOrCreateCanonicalBroker(BrokerResolutionInput.builder()
                .name("Minha Corretora")
                .document("11.222.333/0001-44")
                .build());

        assertThat(created.getBrokerKey()).isEqualTo("BROKER_MINHACORRETORA");
        assertThat(created.getKnownDocuments()).contains("11.222.333/0001-44");
    }
}
