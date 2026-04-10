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

class AssetConversionImpactTranslatorTest {

    private final AssetConversionImpactTranslator translator = new AssetConversionImpactTranslator();

    @Test
    void shouldTranslateAssetConversionIntoDecreaseAndIncreaseImpacts() {
        PortfolioEvent conversion = PortfolioEvent.builder()
                .id("conversion-1")
                .eventType(EventType.ASSET_CONVERSION)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("HFOF11")
                .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                .quantity(10)
                .unitPrice(MonetaryValue.of("120.036907"))
                .totalValue(MonetaryValue.of("1200.369071"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2020, 1, 2))
                .brokerKey("BROKER_CLEAR")
                .sourceReferenceId("ASSET_CONVERSION:FOFT11:HFOF11:2020-01-02:1:0.992479")
                .idempotencyKey("idemp-conversion-1")
                .metadata(PortfolioEventMetadata.builder()
                        .oldTicker("FOFT11")
                        .newTicker("HFOF11")
                        .splitRatio("1:0.992479")
                        .build())
                .createdAt(LocalDateTime.now())
                .build();

        var impacts = translator.translate(conversion);

        assertThat(impacts).hasSize(2);

        assertThat(impacts.get(0).getImpactType()).isEqualTo(PositionImpactType.DECREASE);
        assertThat(impacts.get(0).getTicker()).isEqualTo("FOFT11");
        assertThat(impacts.get(0).getQuantity()).isEqualTo(10);

        assertThat(impacts.get(1).getImpactType()).isEqualTo(PositionImpactType.INCREASE);
        assertThat(impacts.get(1).getTicker()).isEqualTo("HFOF11");
        assertThat(impacts.get(1).getQuantity()).isEqualTo(9);
        assertThat(impacts.get(1).getUnitPrice().toBigDecimal()).isEqualByComparingTo("120.946546");
    }

    @Test
    void shouldTranslateAssetConversionWithoutIncreaseWhenIntegerPartIsZero() {
        PortfolioEvent conversion = PortfolioEvent.builder()
                .id("conversion-2")
                .eventType(EventType.ASSET_CONVERSION)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName("HFOF11")
                .assetType(AssetType.REAL_ESTATE_FUND_BRL)
                .quantity(1)
                .unitPrice(MonetaryValue.of("100"))
                .totalValue(MonetaryValue.of("100"))
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(LocalDate.of(2020, 1, 2))
                .brokerKey("BROKER_CLEAR")
                .sourceReferenceId("ASSET_CONVERSION:FOFT11:HFOF11:2020-01-02:1:0.2")
                .idempotencyKey("idemp-conversion-2")
                .metadata(PortfolioEventMetadata.builder()
                        .oldTicker("FOFT11")
                        .newTicker("HFOF11")
                        .splitRatio("1:0.2")
                        .build())
                .createdAt(LocalDateTime.now())
                .build();

        var impacts = translator.translate(conversion);

        assertThat(impacts).hasSize(1);
        assertThat(impacts.getFirst().getImpactType()).isEqualTo(PositionImpactType.DECREASE);
        assertThat(impacts.getFirst().getTicker()).isEqualTo("FOFT11");
    }
}
