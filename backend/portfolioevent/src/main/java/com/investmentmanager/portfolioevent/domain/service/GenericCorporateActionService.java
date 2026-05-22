package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.port.in.CreateGenericCorporateActionCommand;
import com.investmentmanager.portfolioevent.domain.port.in.GenericCorporateActionUseCase;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class GenericCorporateActionService implements GenericCorporateActionUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;
    private final CanonicalBrokerResolver brokerResolver;

    @Override
    public PortfolioEvent create(CreateGenericCorporateActionCommand command) {
        validate(command);
        String brokerKey = brokerResolver.findOrCreateCanonicalBroker(BrokerResolutionInput.builder()
                        .name(command.getBrokerName())
                        .document(command.getBrokerDocument())
                        .sourceSystem("CORPORATE_ACTION")
                        .sourceReferenceId("GENERIC:" + command.getEventDate())
                        .build())
                .getBrokerKey();

        var assets = collectAssets(command);
        String sourceReferenceId = buildSourceReference(command, assets);
        String targetTicker = command.getTargetAsset() != null ? command.getTargetAsset().getTicker() : assets.getFirst().getTicker();
        var targetType = command.getTargetAsset() != null ? command.getTargetAsset().getAssetType() : assets.getFirst().getAssetType();

        PortfolioEvent event = PortfolioEvent.create(
                EventType.GENERIC_CORPORATE_ACTION,
                EventSource.CORPORATE_ACTION,
                targetTicker,
                targetType,
                1,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                command.getCurrency(),
                command.getEventDate(),
                brokerKey,
                sourceReferenceId,
                null,
                PortfolioEventMetadata.builder()
                        .genericObservation(command.getObservation())
                        .genericAssets(assets.stream().map(asset -> PortfolioEventMetadata.GenericAssetSnapshot.builder()
                                .ticker(asset.getTicker())
                                .assetType(asset.getAssetType().name())
                                .quantity(asset.getQuantity())
                                .averagePrice(asset.getAveragePrice())
                                .build()).toList())
                        .genericResidualAmount(command.getResidual() != null ? command.getResidual().getAmount() : null)
                        .genericResidualDescription(command.getResidual() != null ? command.getResidual().getDescription() : null)
                        .build());

        if (repository.existsByIdempotencyKey(event.getIdempotencyKey())) {
            throw new IllegalStateException("Evento corporativo genérico duplicado para a mesma chave de idempotência");
        }

        PortfolioEvent saved = repository.saveAll(List.of(event)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));
        return saved;
    }

    private List<CreateGenericCorporateActionCommand.AssetTarget> collectAssets(CreateGenericCorporateActionCommand command) {
        List<CreateGenericCorporateActionCommand.AssetTarget> assets = new ArrayList<>();
        if (command.getTargetAsset() != null) assets.add(command.getTargetAsset());
        if (command.getDerivedAssets() != null) assets.addAll(command.getDerivedAssets());
        return assets;
    }

    private String buildSourceReference(CreateGenericCorporateActionCommand command,
                                        List<CreateGenericCorporateActionCommand.AssetTarget> assets) {
        String baseTicker = command.getTargetAsset() != null
                ? command.getTargetAsset().getTicker()
                : assets.getFirst().getTicker();
        return "GENERIC_CA:%s:%s".formatted(
                baseTicker.trim().toUpperCase(Locale.ROOT),
                command.getEventDate());
    }

    private void validate(CreateGenericCorporateActionCommand command) {
        if (command == null) throw new IllegalArgumentException("Command é obrigatório");
        if (command.getEventDate() == null) throw new IllegalArgumentException("Data do evento é obrigatória");
        if (command.getBrokerDocument() == null || command.getBrokerDocument().isBlank()) throw new IllegalArgumentException("Documento da corretora é obrigatório");
        if (command.getObservation() == null || command.getObservation().trim().length() < 50) throw new IllegalArgumentException("Observação é obrigatória e deve ter no mínimo 50 caracteres");
        var assets = collectAssets(command);
        if (assets.isEmpty()) throw new IllegalArgumentException("Informe targetAsset e/ou derivedAssets");
        assets.forEach(asset -> {
            if (asset.getTicker() == null || asset.getTicker().isBlank()) throw new IllegalArgumentException("Ticker do ativo é obrigatório");
            if (asset.getAssetType() == null) throw new IllegalArgumentException("Tipo do ativo é obrigatório");
            if (asset.getQuantity() == null || asset.getQuantity() <= 0) throw new IllegalArgumentException("Quantidade do ativo deve ser > 0");
            if (asset.getAveragePrice() == null || asset.getAveragePrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Preço médio do ativo deve ser >= 0");
        });
    }
}
