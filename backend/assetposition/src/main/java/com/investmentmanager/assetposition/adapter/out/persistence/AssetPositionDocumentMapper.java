package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.AssetPosition;
import com.investmentmanager.assetposition.domain.model.AssetPositionSnapshot;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AssetPositionDocumentMapper {

    static AssetPositionDocument toDocument(AssetPosition position) {
        var doc = new AssetPositionDocument();
        doc.setId(position.getId());
        doc.setAssetName(position.getAssetName());
        doc.setAssetType(position.getAssetType() != null ? position.getAssetType().name() : null);
        doc.setBrokerName(position.getBrokerName());
        doc.setBrokerDocument(position.getBrokerDocument());
        doc.setQuantity(position.getQuantity());
        doc.setAveragePrice(position.getAveragePrice().toDisplayValue());
        doc.setTotalCost(position.getTotalCost().toDisplayValue());
        doc.setCurrency(position.getCurrency());
        doc.setUpdatedAt(position.getUpdatedAt());
        doc.setHistory(position.getHistory() != null
                ? position.getHistory().stream().map(AssetPositionDocumentMapper::toSnapshotDoc).toList()
                : Collections.emptyList());
        return doc;
    }

    static AssetPosition toDomain(AssetPositionDocument doc) {
        return AssetPosition.builder()
                .id(doc.getId())
                .assetName(doc.getAssetName())
                .assetType(doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null)
                .brokerName(doc.getBrokerName())
                .brokerDocument(doc.getBrokerDocument())
                .quantity(doc.getQuantity())
                .averagePrice(MonetaryValue.of(doc.getAveragePrice()))
                .totalCost(MonetaryValue.of(doc.getTotalCost()))
                .currency(doc.getCurrency())
                .updatedAt(doc.getUpdatedAt())
                .history(doc.getHistory() != null
                        ? doc.getHistory().stream().map(AssetPositionDocumentMapper::toSnapshotDomain).toList()
                        : Collections.emptyList())
                .build();
    }

    private static AssetPositionSnapshotDocument toSnapshotDoc(AssetPositionSnapshot snapshot) {
        return new AssetPositionSnapshotDocument(
                snapshot.getQuantity(),
                snapshot.getAveragePrice().toDisplayValue(),
                snapshot.getTotalCost().toDisplayValue(),
                snapshot.getEventDate(),
                snapshot.getSourceType(),
                snapshot.getSourceReferenceId(),
                snapshot.getObservation(),
                snapshot.getRecordedAt());
    }

    private static AssetPositionSnapshot toSnapshotDomain(AssetPositionSnapshotDocument doc) {
        return AssetPositionSnapshot.builder()
                .quantity(doc.getQuantity())
                .averagePrice(MonetaryValue.of(doc.getAveragePrice()))
                .totalCost(MonetaryValue.of(doc.getTotalCost()))
                .eventDate(doc.getEventDate())
                .sourceType(doc.getSourceType())
                .sourceReferenceId(doc.getSourceReferenceId())
                .observation(doc.getObservation())
                .recordedAt(doc.getRecordedAt())
                .build();
    }
}
