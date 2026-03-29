package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.exception.IdempotentOperationException;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSubscriptionCommand;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    @Test
    void shouldRejectDuplicatedSubscriptionOnPreCheck() {
        PortfolioEventRepositoryPort repository = mock(PortfolioEventRepositoryPort.class);
        PositionImpactGenerationService impactGenerationService = mock(PositionImpactGenerationService.class);
        SubscriptionService service = new SubscriptionService(repository, impactGenerationService);

        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder()
                .subscriptionTicker("PETR4-F")
                .targetTicker("PETR4")
                .targetAssetType(AssetType.STOCKS_BRL)
                .quantity(100)
                .unitPrice(new BigDecimal("10.00"))
                .totalValue(new BigDecimal("1000.00"))
                .fee(BigDecimal.ZERO)
                .currency("BRL")
                .brokerName("XP")
                .brokerDocument("123456")
                .subscriptionDate(LocalDate.of(2026, 3, 10))
                .build();

        when(repository.existsSubscriptionByUniqueKey(
                "SUBSCRIPTION",
                "PETR4",
                "STOCKS_BRL",
                "123456",
                LocalDate.of(2026, 3, 10)))
                .thenReturn(true);

        assertThrows(IdempotentOperationException.class, () -> service.create(command));
        verify(repository, never()).saveAll(any());
    }
}
