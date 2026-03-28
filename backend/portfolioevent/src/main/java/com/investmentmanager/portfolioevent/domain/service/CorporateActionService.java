package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.adjustment.Ratio;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CorporateActionUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateCorporateActionCommand;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CorporateActionService implements CorporateActionUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;

    @Override
    public PortfolioEvent createSplit(CreateCorporateActionCommand command) {
        return createCorporateAction(command, EventType.SPLIT);
    }

    @Override
    public PortfolioEvent createReverseSplit(CreateCorporateActionCommand command) {
        return createCorporateAction(command, EventType.REVERSE_SPLIT);
    }

    private PortfolioEvent createCorporateAction(CreateCorporateActionCommand command, EventType eventType) {
        validate(command);

        Ratio ratio = Ratio.builder()
                .numerator(command.getRatioNumerator())
                .denominator(command.getRatioDenominator())
                .build();
        ratio.toFactor();

        PortfolioEvent event = PortfolioEvent.builder()
                .eventType(eventType)
                .eventSource(EventSource.CORPORATE_ACTION)
                .assetName(command.getTicker())
                .assetType(command.getAssetType() != null ? command.getAssetType() : AssetType.STOCKS_BRL)
                .quantity(1)
                .unitPrice(MonetaryValue.zero())
                .totalValue(MonetaryValue.zero())
                .fee(MonetaryValue.zero())
                .currency("BRL")
                .eventDate(command.getEventDate())
                .brokerName(command.getBrokerName() != null ? command.getBrokerName() : "CORPORATE_ACTION")
                .brokerDocument(command.getBrokerDocument() != null ? command.getBrokerDocument() : "CORPORATE_ACTION")
                .sourceReferenceId(eventType.name() + "-" + command.getTicker() + "-" + command.getEventDate())
                .ratio(ratio)
                .createdAt(LocalDateTime.now())
                .build();

        PortfolioEvent saved = repository.saveAll(List.of(event)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));
        return saved;
    }

    private void validate(CreateCorporateActionCommand command) {
        if (command.getTicker() == null || command.getTicker().isBlank()) {
            throw new IllegalArgumentException("Ticker is required");
        }
        if (command.getEventDate() == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (command.getRatioNumerator() == null || command.getRatioDenominator() == null) {
            throw new IllegalArgumentException("Ratio numerator/denominator are required");
        }
    }
}
