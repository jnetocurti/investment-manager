package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.BonusRatio;
import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.port.in.BonusUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateBonusCommand;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class BonusService implements BonusUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;
    private final CanonicalBrokerResolver brokerResolver;

    @Override
    public PortfolioEvent create(CreateBonusCommand command) {
        validate(command);

        String ticker = normalizeTicker(command.getTargetTicker());
        BonusRatio ratio = BonusRatio.parse(command.getRatio());

        String brokerKey = brokerResolver.findOrCreateCanonicalBroker(BrokerResolutionInput.builder()
                        .name(command.getBrokerName())
                        .document(command.getBrokerDocument())
                        .sourceSystem("CORPORATE_ACTION")
                        .sourceReferenceId(ticker + ":" + command.getEventDate())
                        .build())
                .getBrokerKey();

        String sourceReferenceId = "BONUS:%s:%s:%s".formatted(
                ticker,
                command.getEventDate(),
                ratio.canonical());

        // A quantidade efetiva é calculada na projeção de posição, respeitando a linha do tempo do evento.
        int placeholderQuantity = 1;
        BigDecimal placeholderTotalValue = command.getUnitPrice();

        PortfolioEvent bonus = PortfolioEvent.create(
                EventType.BONUS,
                EventSource.CORPORATE_ACTION,
                ticker,
                command.getTargetAssetType(),
                placeholderQuantity,
                command.getUnitPrice(),
                placeholderTotalValue,
                BigDecimal.ZERO,
                command.getCurrency(),
                command.getEventDate(),
                brokerKey,
                sourceReferenceId,
                PortfolioEventMetadata.bonus(ratio.canonical(), null));

        if (repository.existsByIdempotencyKey(bonus.getIdempotencyKey())) {
            throw new IllegalStateException("Bonificação duplicada para a mesma chave de idempotência");
        }

        PortfolioEvent saved = repository.saveAll(List.of(bonus)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));
        return saved;
    }

    private void validate(CreateBonusCommand command) {
        if (command.getTargetTicker() == null || command.getTargetTicker().isBlank()) {
            throw new IllegalArgumentException("Ticker do ativo é obrigatório");
        }
        if (command.getTargetAssetType() == null) {
            throw new IllegalArgumentException("Tipo do ativo é obrigatório");
        }
        if (command.getUnitPrice() == null || command.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor unitário da bonificação deve ser > 0");
        }
        if (command.getEventDate() == null) {
            throw new IllegalArgumentException("Data da bonificação é obrigatória");
        }
        if (command.getBrokerDocument() == null || command.getBrokerDocument().isBlank()) {
            throw new IllegalArgumentException("Documento da corretora é obrigatório");
        }
        BonusRatio.parse(command.getRatio());
    }

    private String normalizeTicker(String ticker) {
        return ticker.trim().toUpperCase(Locale.ROOT);
    }
}
