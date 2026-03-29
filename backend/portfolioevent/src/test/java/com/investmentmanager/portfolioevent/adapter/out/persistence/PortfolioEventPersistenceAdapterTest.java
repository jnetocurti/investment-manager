package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.exception.IdempotentOperationException;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PortfolioEventPersistenceAdapterTest {

    @Test
    void shouldTranslateDuplicateKeyIntoIdempotentDomainError() {
        PortfolioEventMongoRepository mongoRepository = mock(PortfolioEventMongoRepository.class);
        PortfolioEventPersistenceAdapter adapter = new PortfolioEventPersistenceAdapter(mongoRepository);

        PortfolioEvent event = PortfolioEvent.builder()
                .eventType(EventType.SUBSCRIPTION)
                .eventSource(EventSource.SUBSCRIPTION)
                .assetName("PETR4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(10)
                .unitPrice(MonetaryValue.of("10"))
                .totalValue(MonetaryValue.of("100"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 3, 10))
                .brokerName("XP")
                .brokerDocument("123456")
                .sourceReferenceId("ref-1")
                .createdAt(LocalDateTime.now())
                .build();

        when(mongoRepository.saveAll(anyList())).thenThrow(new DuplicateKeyException("dup"));

        assertThrows(IdempotentOperationException.class, () -> adapter.saveAll(List.of(event)));
    }
}
