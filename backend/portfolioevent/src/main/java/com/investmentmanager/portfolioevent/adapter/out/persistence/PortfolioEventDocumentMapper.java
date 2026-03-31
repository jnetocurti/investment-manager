package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
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
        doc.setBrokerKey(event.getBrokerKey());
        doc.setIdempotencyKey(event.getIdempotencyKey());
        doc.setSourceReferenceId(event.getSourceReferenceId());
        doc.setMetadata(toMetadataDocument(event.getMetadata()));
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
                .brokerKey(doc.getBrokerKey())
                .idempotencyKey(doc.getIdempotencyKey())
                .sourceReferenceId(doc.getSourceReferenceId())
                .metadata(toMetadata(doc.getMetadata()))
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private static PortfolioEventDocument.MetadataDocument toMetadataDocument(PortfolioEventMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        var metadataDocument = new PortfolioEventDocument.MetadataDocument();
        metadataDocument.setSubscriptionTicker(metadata.getSubscriptionTicker());
        return metadataDocument;
    }

    private static PortfolioEventMetadata toMetadata(PortfolioEventDocument.MetadataDocument metadataDocument) {
        if (metadataDocument == null) {
            return null;
        }
        return PortfolioEventMetadata.builder()
                .subscriptionTicker(metadataDocument.getSubscriptionTicker())
                .build();
    }
}
