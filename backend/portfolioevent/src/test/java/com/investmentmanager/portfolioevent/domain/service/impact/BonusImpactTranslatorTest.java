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

class BonusImpactTranslatorTest {

    private final BonusImpactTranslator translator = new BonusImpactTranslator();

    @Test
    void shouldTranslateBonusToIncreaseImpact() {
        PortfolioEvent bonus = PortfolioEvent.builder()
                .id("bonus-1")
                .eventType(EventType.BONUS)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("BBDC3")
                .assetType(AssetType.STOCKS_BRL)
                .quantity(5)
                .unitPrice(MonetaryValue.of("4.527177676"))
                .totalValue(MonetaryValue.of("22.635888380"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2021, 4, 23))
                .brokerKey("BROKER_CLEAR")
                .sourceReferenceId("BONUS:BBDC3:2021-04-23:1:10")
                .idempotencyKey("idemp-bonus-1")
                .metadata(PortfolioEventMetadata.bonus("1:10", 50))
                .createdAt(LocalDateTime.now())
                .build();

        var impacts = translator.translate(bonus);

        assertThat(impacts).hasSize(1);
        assertThat(impacts.getFirst().getImpactType()).isEqualTo(PositionImpactType.INCREASE);
        assertThat(impacts.getFirst().getQuantity()).isEqualTo(5);
        assertThat(impacts.getFirst().getUnitPrice().toDisplayValue()).isEqualByComparingTo("4.527177676");
    }
}
