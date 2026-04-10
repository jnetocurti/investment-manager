package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.ConversionRatio;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.port.in.AssetConversionUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateAssetConversionCommand;
import com.investmentmanager.portfolioevent.domain.port.out.AssetPositionQueryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class AssetConversionService implements AssetConversionUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;
    private final CanonicalBrokerResolver brokerResolver;
    private final AssetPositionQueryPort assetPositionQueryPort;

    @Override
    public PortfolioEvent create(CreateAssetConversionCommand command) {
        validate(command);

        String oldTicker = normalizeTicker(command.getOldTicker());
        String newTicker = normalizeTicker(command.getNewTicker());
        ConversionRatio ratio = ConversionRatio.parse(command.getRatio());

        String brokerKey = brokerResolver.findOrCreateCanonicalBroker(BrokerResolutionInput.builder()
                        .name(command.getBrokerName())
                        .document(command.getBrokerDocument())
                        .sourceSystem("CORPORATE_ACTION")
                        .sourceReferenceId(oldTicker + ":" + newTicker + ":" + command.getEventDate())
                        .build())
                .getBrokerKey();

        var sourcePosition = assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey(
                        oldTicker,
                        command.getAssetType(),
                        brokerKey)
                .orElseThrow(() -> new IllegalStateException("Posição de origem não encontrada para o ticker: " + oldTicker));

        if (sourcePosition.quantity() <= 0) {
            throw new IllegalStateException("Posição de origem não possui quantidade ativa para o ticker: " + oldTicker);
        }

        BigDecimal factor = ratio.factor();
        int sourceQuantity = sourcePosition.quantity();
        BigDecimal rawTargetQuantity = BigDecimal.valueOf(sourceQuantity).multiply(factor);
        int convertedQuantity = rawTargetQuantity.setScale(0, RoundingMode.DOWN).intValueExact();

        MonetaryValue sourceAveragePrice = sourcePosition.averagePrice();
        MonetaryValue sourceTotalCost = sourcePosition.totalCost();
        MonetaryValue theoreticalPostConversionPrice = sourceAveragePrice.divide(factor);
        BigDecimal convertedFractionQuantity = rawTargetQuantity.subtract(BigDecimal.valueOf(convertedQuantity));
        MonetaryValue fractionResidualBookValue = theoreticalPostConversionPrice.multiply(convertedFractionQuantity);

        String sourceReferenceId = "ASSET_CONVERSION:%s:%s:%s:%s".formatted(
                oldTicker,
                newTicker,
                command.getEventDate(),
                ratio.canonical());

        PortfolioEvent conversion = PortfolioEvent.create(
                EventType.ASSET_CONVERSION,
                EventSource.CORPORATE_ACTION,
                newTicker,
                command.getAssetType(),
                sourceQuantity,
                sourceAveragePrice.toBigDecimal(),
                sourceTotalCost.toBigDecimal(),
                BigDecimal.ZERO,
                command.getCurrency(),
                command.getEventDate(),
                brokerKey,
                sourceReferenceId,
                PortfolioEventMetadata.builder()
                        .oldTicker(oldTicker)
                        .newTicker(newTicker)
                        .splitRatio(ratio.canonical())
                        .splitFractionResidualBookValue(fractionResidualBookValue.toBigDecimal())
                        .splitFractionFlowStatus("PENDING_SETTLEMENT")
                        .splitFractionSourceReferenceId(sourceReferenceId)
                        .build());

        if (repository.existsByIdempotencyKey(conversion.getIdempotencyKey())) {
            throw new IllegalStateException("Conversão de ativo duplicada para a mesma chave de idempotência");
        }

        PortfolioEvent saved = repository.saveAll(List.of(conversion)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));
        return saved;
    }

    private void validate(CreateAssetConversionCommand command) {
        if (command.getOldTicker() == null || command.getOldTicker().isBlank()) {
            throw new IllegalArgumentException("Ticker de origem é obrigatório");
        }
        if (command.getNewTicker() == null || command.getNewTicker().isBlank()) {
            throw new IllegalArgumentException("Ticker de destino é obrigatório");
        }
        if (normalizeTicker(command.getOldTicker()).equals(normalizeTicker(command.getNewTicker()))) {
            throw new IllegalArgumentException("Ticker de destino deve ser diferente do ticker de origem");
        }
        if (command.getAssetType() == null) {
            throw new IllegalArgumentException("Tipo do ativo é obrigatório");
        }
        if (command.getEventDate() == null) {
            throw new IllegalArgumentException("Data da conversão é obrigatória");
        }
        if (command.getBrokerDocument() == null || command.getBrokerDocument().isBlank()) {
            throw new IllegalArgumentException("Documento da corretora é obrigatório");
        }
        ConversionRatio.parse(command.getRatio());
    }

    private String normalizeTicker(String ticker) {
        return ticker.trim().toUpperCase(Locale.ROOT);
    }
}
