package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.model.SplitRatio;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSplitCommand;
import com.investmentmanager.portfolioevent.domain.port.in.SplitUseCase;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class SplitService implements SplitUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;
    private final CanonicalBrokerResolver brokerResolver;

    @Override
    public PortfolioEvent create(CreateSplitCommand command) {
        validate(command);
        SplitRatio ratio = SplitRatio.parse(command.getRatio());

        String brokerKey = brokerResolver.findOrCreateCanonicalBroker(BrokerResolutionInput.builder()
                        .name(command.getBrokerName())
                        .document(command.getBrokerDocument())
                        .sourceSystem("CORPORATE_ACTION")
                        .sourceReferenceId(command.getTargetTicker() + ":" + command.getEventDate())
                        .build())
                .getBrokerKey();

        String sourceReferenceId = "SPLIT:%s:%s:%s".formatted(
                command.getTargetTicker(),
                command.getEventDate(),
                ratio.canonical());

        PortfolioEvent split = PortfolioEvent.create(
                EventType.SPLIT,
                EventSource.CORPORATE_ACTION,
                command.getTargetTicker(),
                command.getTargetAssetType(),
                1,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                command.getCurrency(),
                command.getEventDate(),
                brokerKey,
                sourceReferenceId,
                PortfolioEventMetadata.split(ratio.canonical(), sourceReferenceId));

        if (repository.existsByIdempotencyKey(split.getIdempotencyKey())) {
            throw new IllegalStateException("Split duplicado para a mesma chave de idempotência");
        }

        PortfolioEvent saved = repository.saveAll(List.of(split)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));
        return saved;
    }

    private void validate(CreateSplitCommand command) {
        if (command.getTargetTicker() == null || command.getTargetTicker().isBlank())
            throw new IllegalArgumentException("Ticker do ativo é obrigatório");
        if (command.getTargetAssetType() == null)
            throw new IllegalArgumentException("Tipo do ativo é obrigatório");
        if (command.getEventDate() == null)
            throw new IllegalArgumentException("Data do split é obrigatória");
        if (command.getBrokerDocument() == null || command.getBrokerDocument().isBlank())
            throw new IllegalArgumentException("Documento da corretora é obrigatório");
        SplitRatio.parse(command.getRatio());
    }
}
