package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.adjustment.Ratio;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class PortfolioEventDocumentMapper {

    static PortfolioEventDocument toDocument(PortfolioEvent event) {
        var doc = new PortfolioEventDocument();
        doc.setEventType(event.getEventType().name());
        doc.setEventSource(event.getEventSource().name());
        doc.setAssetName(event.getAssetName());
        doc.setAssetType(event.getAssetType() != null ? event.getAssetType().name() : null);
        doc.setQuantity(event.getQuantity());
        doc.setUnitPrice(event.getUnitPrice() != null ? event.getUnitPrice().toDisplayValue() : BigDecimal.ZERO);
        doc.setTotalValue(event.getTotalValue() != null ? event.getTotalValue().toDisplayValue() : BigDecimal.ZERO);
        doc.setFee(event.getFee() != null ? event.getFee().toDisplayValue() : BigDecimal.ZERO);
        doc.setCurrency(event.getCurrency());
        doc.setEventDate(event.getEventDate());
        doc.setBrokerName(event.getBrokerName());
        doc.setBrokerDocument(event.getBrokerDocument());
        doc.setSourceReferenceId(event.getSourceReferenceId());
        doc.setSubscriptionTicker(event.getSubscriptionTicker());
        if (event.getRatio() != null) {
            doc.setRatioNumerator(event.getRatio().getNumerator());
            doc.setRatioDenominator(event.getRatio().getDenominator());
        }
        doc.setCreatedAt(event.getCreatedAt());
        return doc;
    }

    static PortfolioEvent toDomain(PortfolioEventDocument doc) {
        Ratio ratio = null;
        if (doc.getRatioNumerator() != null && doc.getRatioDenominator() != null) {
            ratio = Ratio.builder()
                    .numerator(doc.getRatioNumerator())
                    .denominator(doc.getRatioDenominator())
                    .build();
        }

        return PortfolioEvent.builder()
                .id(doc.getId())
                .eventType(EventType.valueOf(doc.getEventType()))
                .eventSource(EventSource.valueOf(doc.getEventSource()))
                .assetName(doc.getAssetName())
                .assetType(doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null)
                .quantity(doc.getQuantity())
                .unitPrice(MonetaryValue.of(doc.getUnitPrice() != null ? doc.getUnitPrice() : BigDecimal.ZERO))
                .totalValue(MonetaryValue.of(doc.getTotalValue() != null ? doc.getTotalValue() : BigDecimal.ZERO))
                .fee(MonetaryValue.of(doc.getFee() != null ? doc.getFee() : BigDecimal.ZERO))
                .currency(doc.getCurrency())
                .eventDate(doc.getEventDate())
                .brokerName(doc.getBrokerName())
                .brokerDocument(doc.getBrokerDocument())
                .sourceReferenceId(doc.getSourceReferenceId())
                .subscriptionTicker(doc.getSubscriptionTicker())
                .ratio(ratio)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
