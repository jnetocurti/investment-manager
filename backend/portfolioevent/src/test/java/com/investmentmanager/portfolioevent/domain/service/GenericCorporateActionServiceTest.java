package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.CanonicalBroker;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateGenericCorporateActionCommand;
import com.investmentmanager.portfolioevent.domain.port.out.BrokerCatalogRepositoryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GenericCorporateActionServiceTest {

    @Test
    void shouldCreateGenericCorporateAction() {
        PortfolioEventRepositoryPort repository = Mockito.mock(PortfolioEventRepositoryPort.class);
        PositionImpactGenerationService impactService = Mockito.mock(PositionImpactGenerationService.class);
        BrokerCatalogRepositoryPort brokerRepo = Mockito.mock(BrokerCatalogRepositoryPort.class);
        CanonicalBrokerResolver resolver = new CanonicalBrokerResolver(brokerRepo);

        when(brokerRepo.findByBrokerKey(any())).thenReturn(java.util.Optional.empty());
        when(brokerRepo.save(any())).thenReturn(CanonicalBroker.builder().brokerKey("B3").build());
        when(repository.existsByIdempotencyKey(any())).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        GenericCorporateActionService service = new GenericCorporateActionService(repository, impactService, resolver);

        var cmd = CreateGenericCorporateActionCommand.builder()
                .eventDate(LocalDate.of(2024, 4, 16))
                .brokerDocument("00.000.000/0000-00")
                .brokerName("XP")
                .currency("BRL")
                .observation("Evento excepcional para ajuste manual determinístico de ativos na carteira.")
                .targetAsset(CreateGenericCorporateActionCommand.AssetTarget.builder().ticker("BRCR11").assetType(AssetType.FII).quantity(88).averagePrice(new BigDecimal("80.2")).build())
                .derivedAssets(List.of(CreateGenericCorporateActionCommand.AssetTarget.builder().ticker("CNES11").assetType(AssetType.FII).quantity(88).averagePrice(new BigDecimal("7.7")).build()))
                .build();

        PortfolioEvent event = service.create(cmd);
        assertNotNull(event);
        assertEquals("GENERIC_CORPORATE_ACTION", event.getEventType().name());
        assertEquals(2, event.getMetadata().getGenericAssets().size());
    }

    @Test
    void shouldFailWhenObservationTooShort() {
        GenericCorporateActionService service = new GenericCorporateActionService(Mockito.mock(PortfolioEventRepositoryPort.class), Mockito.mock(PositionImpactGenerationService.class), Mockito.mock(CanonicalBrokerResolver.class));
        var cmd = CreateGenericCorporateActionCommand.builder().observation("curta").build();
        assertThrows(IllegalArgumentException.class, () -> service.create(cmd));
    }
}
