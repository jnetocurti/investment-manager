package com.investmentmanager.portfolioevent.domain.service.impact;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TickerRenameImpactTranslatorTest {

    private final TickerRenameImpactTranslator translator = new TickerRenameImpactTranslator();

    @Test
    void shouldTranslateTickerRenameIntoDecreaseAndIncreaseImpacts() {
        PortfolioEvent rename = PortfolioEvent.builder()
                .id("rename-1")
                .eventType(EventType.TICKER_RENAME)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("GGRC11")
                .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                .quantity(25)
                .unitPrice(MonetaryValue.of("100"))
                .totalValue(MonetaryValue.of("2500"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2026, 4, 1))
                .brokerKey("BROKER_CLEAR")
                .sourceReferenceId("TICKER_RENAME:GETT11:GGRC11:2026-04-01")
                .idempotencyKey("idemp-rename-1")
                .metadata(PortfolioEventMetadata.tickerRename("GETT11", "GGRC11"))
                .createdAt(LocalDateTime.now())
                .build();

        var impacts = translator.translate(rename);

        assertThat(impacts).hasSize(2);
        assertThat(impacts.get(0).getImpactType()).isEqualTo(PositionImpactType.DECREASE);
        assertThat(impacts.get(0).getTicker()).isEqualTo("GETT11");
        assertThat(impacts.get(0).getSequence()).isEqualTo(1);
        assertThat(impacts.get(0).getQuantity()).isEqualTo(25);

        assertThat(impacts.get(1).getImpactType()).isEqualTo(PositionImpactType.INCREASE);
        assertThat(impacts.get(1).getTicker()).isEqualTo("GGRC11");
        assertThat(impacts.get(1).getSequence()).isEqualTo(2);
        assertThat(impacts.get(1).getQuantity()).isEqualTo(25);
        assertThat(impacts.get(1).getUnitPrice().toDisplayValue()).isEqualByComparingTo("100");
    }
}
