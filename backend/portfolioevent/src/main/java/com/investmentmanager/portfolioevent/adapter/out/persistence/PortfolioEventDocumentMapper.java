package com.investmentmanager.portfolioevent.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

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
        doc.setEventOrder(event.getEventOrder());
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
                .eventOrder(doc.getEventOrder() != null ? doc.getEventOrder() : 1)
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
        metadataDocument.setSplitRatio(metadata.getSplitRatio());
        metadataDocument.setOldTicker(metadata.getOldTicker());
        metadataDocument.setNewTicker(metadata.getNewTicker());
        metadataDocument.setBonusRatio(metadata.getBonusRatio());
        metadataDocument.setBonusBaseQuantity(metadata.getBonusBaseQuantity());
        metadataDocument.setSplitFractionResidualBookValue(metadata.getSplitFractionResidualBookValue());
        metadataDocument.setSplitFractionFlowStatus(metadata.getSplitFractionFlowStatus());
        metadataDocument.setSplitFractionSourceReferenceId(metadata.getSplitFractionSourceReferenceId());
        metadataDocument.setGenericObservation(metadata.getGenericObservation());
        metadataDocument.setGenericAssets(metadata.getGenericAssets() == null ? null : metadata.getGenericAssets().stream()
                .map(asset -> {
                    var doc = new PortfolioEventDocument.GenericAssetSnapshotDocument();
                    doc.setTicker(asset.getTicker());
                    doc.setAssetType(asset.getAssetType());
                    doc.setQuantity(asset.getQuantity());
                    doc.setAveragePrice(asset.getAveragePrice());
                    return doc;
                }).toList());
        metadataDocument.setGenericResidualAmount(metadata.getGenericResidualAmount());
        metadataDocument.setGenericResidualDescription(metadata.getGenericResidualDescription());
        return metadataDocument;
    }

    private static PortfolioEventMetadata toMetadata(PortfolioEventDocument.MetadataDocument metadataDocument) {
        if (metadataDocument == null) {
            return null;
        }
        return PortfolioEventMetadata.builder()
                .subscriptionTicker(metadataDocument.getSubscriptionTicker())
                .splitRatio(metadataDocument.getSplitRatio())
                .oldTicker(metadataDocument.getOldTicker())
                .newTicker(metadataDocument.getNewTicker())
                .bonusRatio(metadataDocument.getBonusRatio())
                .bonusBaseQuantity(metadataDocument.getBonusBaseQuantity())
                .splitFractionResidualBookValue(metadataDocument.getSplitFractionResidualBookValue())
                .splitFractionFlowStatus(metadataDocument.getSplitFractionFlowStatus())
                .splitFractionSourceReferenceId(metadataDocument.getSplitFractionSourceReferenceId())
                .genericObservation(metadataDocument.getGenericObservation())
                .genericAssets(metadataDocument.getGenericAssets() == null ? null : metadataDocument.getGenericAssets().stream()
                        .map(asset -> PortfolioEventMetadata.GenericAssetSnapshot.builder()
                                .ticker(asset.getTicker())
                                .assetType(asset.getAssetType())
                                .quantity(asset.getQuantity())
                                .averagePrice(asset.getAveragePrice())
                                .build())
                        .toList())
                .genericResidualAmount(metadataDocument.getGenericResidualAmount())
                .genericResidualDescription(metadataDocument.getGenericResidualDescription())
                .build();
    }
}
