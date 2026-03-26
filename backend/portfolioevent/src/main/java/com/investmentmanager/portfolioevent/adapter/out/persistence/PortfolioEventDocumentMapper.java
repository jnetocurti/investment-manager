package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class PortfolioEventDocumentMapper {

    static PortfolioEventDocument toDocument(PortfolioEvent event) {
        var doc = new PortfolioEventDocument();
        doc.setEventType(event.getEventType().name());
        doc.setEventSource(event.getEventSource().name());
        doc.setAssetName(event.getAssetName());
        doc.setAssetType(event.getAssetType() != null ? event.getAssetType().name() : null);
        doc.setQuantity(event.getQuantity());
        doc.setUnitPrice(event.getUnitPrice().toDisplayValue());
        doc.setTotalValue(event.getTotalValue().toDisplayValue());
        doc.setFee(event.getFee().toDisplayValue());
        doc.setCurrency(event.getCurrency());
        doc.setEventDate(event.getEventDate());
        doc.setBrokerName(event.getBrokerName());
        doc.setBrokerDocument(event.getBrokerDocument());
        doc.setSourceReferenceId(event.getSourceReferenceId());
        doc.setSubscriptionTicker(event.getSubscriptionTicker());
        doc.setCreatedAt(event.getCreatedAt());
        return doc;
    }

    static PortfolioEvent toDomain(PortfolioEventDocument doc) {
        return PortfolioEvent.builder()
                .id(doc.getId())
                .eventType(EventType.valueOf(doc.getEventType()))
                .eventSource(EventSource.valueOf(doc.getEventSource()))
                .assetName(doc.getAssetName())
                .assetType(doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null)
                .quantity(doc.getQuantity())
                .unitPrice(MonetaryValue.of(doc.getUnitPrice()))
                .totalValue(MonetaryValue.of(doc.getTotalValue()))
                .fee(MonetaryValue.of(doc.getFee()))
                .currency(doc.getCurrency())
                .eventDate(doc.getEventDate())
                .brokerName(doc.getBrokerName())
                .brokerDocument(doc.getBrokerDocument())
                .sourceReferenceId(doc.getSourceReferenceId())
                .subscriptionTicker(doc.getSubscriptionTicker())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
