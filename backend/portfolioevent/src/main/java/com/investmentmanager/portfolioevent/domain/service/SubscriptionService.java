package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.portfolioevent.domain.model.BrokerResolutionInput;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSubscriptionCommand;
import com.investmentmanager.portfolioevent.domain.port.in.SubscriptionUseCase;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;
    private final CanonicalBrokerResolver brokerResolver;

    @Override
    public PortfolioEvent create(CreateSubscriptionCommand command) {
        validate(command);
        String brokerKey = brokerResolver.findOrCreateCanonicalBroker(BrokerResolutionInput.builder()
                        .name(command.getBrokerName())
                        .document(command.getBrokerDocument())
                        .sourceSystem("SUBSCRIPTION")
                        .sourceReferenceId(command.getTargetTicker() + ":" + command.getSubscriptionDate())
                        .build())
                .getBrokerKey();

        String sourceReferenceId = "SUBSCRIPTION:%s:%s".formatted(command.getTargetTicker(), command.getSubscriptionDate());
        PortfolioEvent subscription = PortfolioEvent.create(
                EventType.SUBSCRIPTION,
                EventSource.SUBSCRIPTION,
                command.getTargetTicker(),
                command.getTargetAssetType(),
                command.getQuantity(),
                command.getUnitPrice(),
                command.getTotalValue(),
                command.getFee(),
                command.getCurrency(),
                command.getSubscriptionDate(),
                brokerKey,
                sourceReferenceId,
                PortfolioEventMetadata.subscription(command.getSubscriptionTicker()));

        if (repository.existsByIdempotencyKey(subscription.getIdempotencyKey())) {
            throw new IllegalStateException("Subscrição duplicada para a mesma chave de idempotência");
        }

        PortfolioEvent saved = repository.saveAll(List.of(subscription)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));

        if (command.getConversionDate() != null) {
            return confirmConversion(saved.getId(), command.getConversionDate());
        }

        return saved;
    }

    @Override
    public PortfolioEvent confirmConversion(String subscriptionEventId, LocalDate conversionDate) {
        PortfolioEvent subscription = repository.findById(subscriptionEventId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Evento de subscrição não encontrado: " + subscriptionEventId));

        if (subscription.getEventType() != EventType.SUBSCRIPTION) {
            throw new IllegalArgumentException(
                    "Evento não é uma subscrição: " + subscriptionEventId);
        }

        if (conversionDate.isBefore(subscription.getEventDate())) {
            throw new IllegalArgumentException(
                    "Data de conversão deve ser >= data de subscrição");
        }

        PortfolioEvent conversion = PortfolioEvent.create(
                EventType.SUBSCRIPTION_CONVERSION,
                EventSource.SUBSCRIPTION,
                subscription.getAssetName(),
                subscription.getAssetType(),
                subscription.getQuantity(),
                subscription.getUnitPrice().toDisplayValue(),
                subscription.getTotalValue().toDisplayValue(),
                subscription.getFee().toDisplayValue(),
                subscription.getCurrency(),
                conversionDate,
                subscription.getBrokerKey(),
                subscriptionEventId,
                subscription.getMetadata());

        if (repository.existsByIdempotencyKey(conversion.getIdempotencyKey())) {
            throw new IllegalStateException("Subscrição já convertida: " + subscriptionEventId);
        }

        PortfolioEvent saved = repository.saveAll(List.of(conversion)).getFirst();
        impactGenerationService.generateAndPublish(List.of(saved));

        log.info("Subscrição convertida: subId={}, target={}, conversionDate={}",
                subscriptionEventId, subscription.getAssetName(), conversionDate);
        return saved;
    }

    private void validate(CreateSubscriptionCommand command) {
        if (command.getSubscriptionTicker() == null || command.getSubscriptionTicker().isBlank())
            throw new IllegalArgumentException("Ticker da subscrição é obrigatório");
        if (command.getTargetTicker() == null || command.getTargetTicker().isBlank())
            throw new IllegalArgumentException("Ticker do ativo final é obrigatório");
        if (command.getTargetAssetType() == null)
            throw new IllegalArgumentException("Tipo do ativo final é obrigatório");
        if (command.getQuantity() <= 0)
            throw new IllegalArgumentException("Quantidade deve ser > 0");
        if (command.getUnitPrice() == null)
            throw new IllegalArgumentException("Valor unitário é obrigatório");
        if (command.getSubscriptionDate() == null)
            throw new IllegalArgumentException("Data de subscrição é obrigatória");
        if (command.getBrokerDocument() == null || command.getBrokerDocument().isBlank())
            throw new IllegalArgumentException("Documento da corretora é obrigatório");
        if (command.getConversionDate() != null
                && command.getConversionDate().isBefore(command.getSubscriptionDate())) {
            throw new IllegalArgumentException(
                    "Data de conversão deve ser >= data de subscrição");
        }
    }
}
