package com.investmentmanager.portfolioevent.domain.service;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.domain.model.EventSource;
import com.investmentmanager.portfolioevent.domain.model.EventType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.model.SubscriptionConversionPortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.model.SubscriptionPortfolioEventMetadata;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSubscriptionCommand;
import com.investmentmanager.portfolioevent.domain.port.in.SubscriptionUseCase;
import com.investmentmanager.portfolioevent.domain.port.out.PortfolioEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionUseCase {

    private final PortfolioEventRepositoryPort repository;
    private final PositionImpactGenerationService impactGenerationService;

    @Override
    public PortfolioEvent create(CreateSubscriptionCommand command) {
        validate(command);

        PortfolioEvent subscription = PortfolioEvent.builder()
                .eventType(EventType.SUBSCRIPTION)
                .eventSource(EventSource.SUBSCRIPTION)
                .assetName(command.getTargetTicker())
                .assetType(command.getTargetAssetType())
                .metadata(new SubscriptionPortfolioEventMetadata(command.getSubscriptionTicker()))
                .quantity(command.getQuantity())
                .unitPrice(MonetaryValue.of(command.getUnitPrice()))
                .totalValue(MonetaryValue.of(command.getTotalValue()))
                .fee(MonetaryValue.of(command.getFee()))
                .currency(command.getCurrency() != null ? command.getCurrency() : "BRL")
                .eventDate(command.getSubscriptionDate())
                .brokerName(command.getBrokerName())
                .brokerDocument(command.getBrokerDocument())
                .sourceReferenceId(command.getSubscriptionTicker() + "-" + command.getSubscriptionDate())
                .createdAt(LocalDateTime.now())
                .build();

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

        if (repository.existsBySourceReferenceId(subscriptionEventId)) {
            throw new IllegalStateException(
                    "Subscrição já convertida: " + subscriptionEventId);
        }

        if (conversionDate.isBefore(subscription.getEventDate())) {
            throw new IllegalArgumentException(
                    "Data de conversão deve ser >= data de subscrição");
        }

        PortfolioEvent conversion = PortfolioEvent.builder()
                .eventType(EventType.SUBSCRIPTION_CONVERSION)
                .eventSource(EventSource.SUBSCRIPTION)
                .assetName(subscription.getAssetName())
                .assetType(subscription.getAssetType())
                .metadata(new SubscriptionConversionPortfolioEventMetadata(subscription.subscriptionTicker()
                        .orElseThrow(() -> new IllegalStateException("Subscrição sem metadata de ticker"))))
                .quantity(subscription.getQuantity())
                .unitPrice(subscription.getUnitPrice())
                .totalValue(subscription.getTotalValue())
                .fee(subscription.getFee())
                .currency(subscription.getCurrency())
                .eventDate(conversionDate)
                .brokerName(subscription.getBrokerName())
                .brokerDocument(subscription.getBrokerDocument())
                .sourceReferenceId(subscriptionEventId)
                .createdAt(LocalDateTime.now())
                .build();

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
