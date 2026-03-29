package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.ImpactSourceType;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CorporateActionUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSplitCorporateActionCommand;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventPublisherPort;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CorporateActionService implements CorporateActionUseCase {

    private final PositionImpactEventRepositoryPort impactRepository;
    private final PositionImpactEventPublisherPort impactPublisher;

    @Override
    public PositionImpactEvent createSplit(CreateSplitCorporateActionCommand command) {
        validate(command);

        PositionImpactEvent impact = PositionImpactEvent.builder()
                .originalEventId(command.getOriginalEventId() != null && !command.getOriginalEventId().isBlank()
                        ? command.getOriginalEventId()
                        : "split-" + command.getTicker() + "-" + command.getEventDate())
                .ticker(command.getTicker())
                .assetType(command.getAssetType())
                .impactType(PositionImpactType.ADJUST)
                .adjustmentType(PositionAdjustmentType.SPLIT)
                .sequence(1)
                .quantity(1)
                .unitPrice(MonetaryValue.zero())
                .fee(MonetaryValue.of(command.getFee() != null ? command.getFee() : java.math.BigDecimal.ZERO))
                .factor(command.getFactor())
                .eventDate(command.getEventDate())
                .originType(EventType.BUY)
                .sourceType(ImpactSourceType.CORPORATE_ACTION)
                .brokerName(command.getBrokerName())
                .brokerDocument(command.getBrokerDocument())
                .sourceReferenceId(command.getSourceReferenceId() != null && !command.getSourceReferenceId().isBlank()
                        ? command.getSourceReferenceId()
                        : "corporate-split-" + UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        impact.validate();

        if (impactRepository.existsByUniqueKey(impact.getOriginalEventId(), impact.getImpactType().name(), impact.getSequence())) {
            throw new IllegalStateException("Split já processado para originalEventId=" + impact.getOriginalEventId());
        }

        List<PositionImpactEvent> persisted = impactRepository.saveAll(List.of(impact));
        impactPublisher.publishAll(persisted);
        return persisted.getFirst();
    }

    private void validate(CreateSplitCorporateActionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command is required");
        }
        if (command.getTicker() == null || command.getTicker().isBlank()) {
            throw new IllegalArgumentException("Ticker is required");
        }
        if (command.getAssetType() == null) {
            throw new IllegalArgumentException("Asset type is required");
        }
        if (command.getEventDate() == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (command.getBrokerName() == null || command.getBrokerName().isBlank()) {
            throw new IllegalArgumentException("Broker name is required");
        }
        if (command.getBrokerDocument() == null || command.getBrokerDocument().isBlank()) {
            throw new IllegalArgumentException("Broker document is required");
        }
        if (command.getFactor() == null || command.getFactor().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Factor must be > 0");
        }
    }
}
