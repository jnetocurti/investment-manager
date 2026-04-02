package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SplitImpactTranslatorTest {

    private final SplitImpactTranslator translator = new SplitImpactTranslator();

    @Test
    void shouldTranslateSplitToAdjustImpact() {
        PortfolioEvent split = PortfolioEvent.builder()
                .id("split-1")
                .eventType(EventType.SPLIT)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("ITSA4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(1)
                .unitPrice(MonetaryValue.zero())
                .totalValue(MonetaryValue.zero())
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 3, 31))
                .brokerKey("BROKER_XP")
                .sourceReferenceId("SPLIT:ITSA4:2026-03-31:1:2")
                .idempotencyKey("idemp")
                .metadata(PortfolioEventMetadata.split("1:2", "SPLIT:ITSA4:2026-03-31:1:2"))
                .createdAt(LocalDateTime.now())
                .build();

        var impacts = translator.translate(split);

        assertThat(impacts).hasSize(1);
        assertThat(impacts.getFirst().getImpactType()).isEqualTo(PositionImpactType.ADJUST);
        assertThat(impacts.getFirst().getAdjustmentType()).isEqualTo(PositionAdjustmentType.SPLIT);
        assertThat(impacts.getFirst().getFactor()).isEqualByComparingTo("2");
    }

    @Test
    void shouldTranslateReverseSplitToAdjustImpact() {
        PortfolioEvent split = PortfolioEvent.builder()
                .id("split-2")
                .eventType(EventType.SPLIT)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("ABCB4")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(1)
                .unitPrice(MonetaryValue.zero())
                .totalValue(MonetaryValue.zero())
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 3, 31))
                .brokerKey("BROKER_XP")
                .sourceReferenceId("SPLIT:ABCB4:2026-03-31:10:1")
                .idempotencyKey("idemp2")
                .metadata(PortfolioEventMetadata.split("10:1", "SPLIT:ABCB4:2026-03-31:10:1"))
                .createdAt(LocalDateTime.now())
                .build();

        var impacts = translator.translate(split);

        assertThat(impacts).hasSize(1);
        assertThat(impacts.getFirst().getImpactType()).isEqualTo(PositionImpactType.ADJUST);
        assertThat(impacts.getFirst().getAdjustmentType()).isEqualTo(PositionAdjustmentType.REVERSE_SPLIT);
        assertThat(impacts.getFirst().getFactor()).isEqualByComparingTo("0.1");
    }
}
