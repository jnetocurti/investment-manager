package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSplitCorporateActionCommand;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CorporateActionServiceTest {

    @Test
    void shouldCreateAndPublishSplitImpact() {
        PositionImpactEventRepositoryPort repository = mock(PositionImpactEventRepositoryPort.class);
        PositionImpactEventPublisherPort publisher = mock(PositionImpactEventPublisherPort.class);
        CorporateActionService service = new CorporateActionService(repository, publisher);

        CreateSplitCorporateActionCommand command = CreateSplitCorporateActionCommand.builder()
                .ticker("ITSA4")
                .assetType(AssetType.STOCKS_BRL)
                .factor(BigDecimal.valueOf(2))
                .fee(BigDecimal.ZERO)
                .eventDate(LocalDate.of(2024, 8, 10))
                .brokerName("XP")
                .brokerDocument("123")
                .sourceReferenceId("split-itsa4-src")
                .originalEventId("split-itsa4-2024")
                .build();

        when(repository.existsByUniqueKey("split-itsa4-2024", "ADJUST", 1)).thenReturn(false);
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PositionImpactEvent event = service.createSplit(command);

        assertThat(event.getImpactType()).isEqualTo(PositionImpactType.ADJUST);
        assertThat(event.getAdjustmentType()).isEqualTo(PositionAdjustmentType.SPLIT);
        assertThat(event.getFactor()).isEqualByComparingTo("2");
        verify(publisher).publishAll(anyList());
    }

    @Test
    void shouldRejectDuplicateSplitByIdempotencyKey() {
        PositionImpactEventRepositoryPort repository = mock(PositionImpactEventRepositoryPort.class);
        PositionImpactEventPublisherPort publisher = mock(PositionImpactEventPublisherPort.class);
        CorporateActionService service = new CorporateActionService(repository, publisher);

        CreateSplitCorporateActionCommand command = CreateSplitCorporateActionCommand.builder()
                .ticker("ITSA4")
                .assetType(AssetType.STOCKS_BRL)
                .factor(BigDecimal.valueOf(2))
                .eventDate(LocalDate.of(2024, 8, 10))
                .brokerName("XP")
                .brokerDocument("123")
                .originalEventId("split-itsa4-2024")
                .build();

        when(repository.existsByUniqueKey("split-itsa4-2024", "ADJUST", 1)).thenReturn(true);

        assertThatThrownBy(() -> service.createSplit(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Split já processado");

        verify(repository, never()).saveAll(any());
        verifyNoInteractions(publisher);
    }
}
