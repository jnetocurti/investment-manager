package com.investmentmanager.assetposition.adapter.out.query;

import com.investmentmanager.assetposition.domain.model.PortfolioEventData;
import com.investmentmanager.assetposition.domain.port.out.PortfolioEventQueryPort;
import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.adapter.out.persistence.PortfolioEventMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class PortfolioEventQueryAdapter implements PortfolioEventQueryPort {

    private final PortfolioEventMongoRepository portfolioEventRepository;

    @Override
    public List<PortfolioEventData> findByAssetNameAndBrokerIdOrderByEventDate(
            String assetName, String brokerId) {
        return portfolioEventRepository
                .findByAssetNameAndBrokerIdOrderByEventDateAsc(assetName, brokerId)
                .stream()
                .map(doc -> PortfolioEventData.builder()
                        .id(doc.getId())
                        .eventType(doc.getEventType())
                        .assetName(doc.getAssetName())
                        .assetType(doc.getAssetType() != null ? AssetType.valueOf(doc.getAssetType()) : null)
                        .brokerId(doc.getBrokerId())
                        .quantity(doc.getQuantity())
                        .totalValue(MonetaryValue.of(doc.getTotalValue()))
                        .fee(MonetaryValue.of(doc.getFee()))
                        .eventDate(doc.getEventDate())
                        .sourceType(doc.getEventSource())
                        .sourceReferenceId(doc.getSourceReferenceId())
                        .build())
                .toList();
    }
}
