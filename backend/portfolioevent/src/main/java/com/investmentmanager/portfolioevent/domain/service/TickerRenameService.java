package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.port.in.CreateTickerRenameCommand;
import com.investmentmanager.portfolioevent.domain.port.in.TickerRenameUseCase;
import com.investmentmanager.portfolioevent.domain.port.out.AssetPositionQueryPort;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class TickerRenameService implements TickerRenameUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;
    private final CanonicalBrokerResolver brokerResolver;
    private final AssetPositionQueryPort assetPositionQueryPort;

    @Override
    public PortfolioEvent create(CreateTickerRenameCommand command) {
        validate(command);

        String oldTicker = normalizeTicker(command.getOldTicker());
        String newTicker = normalizeTicker(command.getNewTicker());

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

        assetPositionQueryPort.findByAssetNameAndAssetTypeAndBrokerKey(newTicker, command.getAssetType(), brokerKey)
                .filter(position -> position.quantity() > 0)
                .ifPresent(position -> {
                    throw new IllegalStateException("Já existe posição ativa para o ticker de destino: " + newTicker);
                });

        String sourceReferenceId = "TICKER_RENAME:%s:%s:%s".formatted(oldTicker, newTicker, command.getEventDate());
        BigDecimal averagePrice = sourcePosition.averagePrice().toDisplayValue();
        int quantity = sourcePosition.quantity();

        PortfolioEvent renameEvent = PortfolioEvent.create(
                EventType.TICKER_RENAME,
                EventSource.CORPORATE_ACTION,
                newTicker,
                command.getAssetType(),
                quantity,
                averagePrice,
                averagePrice.multiply(BigDecimal.valueOf(quantity)),
                BigDecimal.ZERO,
                command.getCurrency(),
                command.getEventDate(),
                brokerKey,
                sourceReferenceId,
                PortfolioEventMetadata.tickerRename(oldTicker, newTicker));

        if (repository.existsByIdempotencyKey(renameEvent.getIdempotencyKey())) {
            throw new IllegalStateException("Troca de ticker duplicada para a mesma chave de idempotência");
        }

        PortfolioEvent saved = repository.saveAll(List.of(renameEvent)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));
        return saved;
    }

    private void validate(CreateTickerRenameCommand command) {
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
            throw new IllegalArgumentException("Data da troca de ticker é obrigatória");
        }
        if (command.getBrokerDocument() == null || command.getBrokerDocument().isBlank()) {
            throw new IllegalArgumentException("Documento da corretora é obrigatório");
        }
    }

    private String normalizeTicker(String ticker) {
        return ticker.trim().toUpperCase(Locale.ROOT);
    }
}
