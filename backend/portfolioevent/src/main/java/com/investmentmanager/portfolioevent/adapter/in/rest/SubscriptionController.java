package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSubscriptionCommand;
import com.investmentmanager.portfolioevent.domain.port.in.SubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionUseCase subscriptionUseCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        try {
            var command = CreateSubscriptionCommand.builder()
                    .subscriptionTicker(request.subscriptionTicker())
                    .targetTicker(request.targetTicker())
                    .targetAssetType(request.targetAssetType() != null
                            ? AssetType.valueOf(request.targetAssetType()) : null)
                    .quantity(request.quantity())
                    .unitPrice(request.unitPrice())
                    .totalValue(request.totalValue())
                    .fee(request.fee() != null ? request.fee() : BigDecimal.ZERO)
                    .currency(request.currency())
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .subscriptionDate(request.subscriptionDate())
                    .conversionDate(request.conversionDate())
                    .build();

            PortfolioEvent result = subscriptionUseCase.create(command);
            return ResponseEntity.ok(toResponse(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/convert")
    public ResponseEntity<?> confirmConversion(@PathVariable String id,
                                                @RequestBody ConvertRequest request) {
        try {
            PortfolioEvent result = subscriptionUseCase.confirmConversion(id, request.conversionDate());
            return ResponseEntity.ok(toResponse(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    private SubscriptionResponse toResponse(PortfolioEvent event) {
        return new SubscriptionResponse(
                event.getId(),
                event.getEventType().name(),
                event.subscriptionTicker().orElse(null),
                event.getAssetName(),
                event.getQuantity(),
                event.getUnitPrice().toString(),
                event.getTotalValue().toString(),
                event.getFee().toString(),
                event.getBrokerName(),
                event.getEventDate().toString());
    }

    record CreateRequest(
            String subscriptionTicker,
            String targetTicker,
            String targetAssetType,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalValue,
            BigDecimal fee,
            String currency,
            String brokerName,
            String brokerDocument,
            LocalDate subscriptionDate,
            LocalDate conversionDate
    ) {}

    record ConvertRequest(LocalDate conversionDate) {}

    record SubscriptionResponse(
            String id,
            String eventType,
            String subscriptionTicker,
            String targetTicker,
            int quantity,
            String unitPrice,
            String totalValue,
            String fee,
            String brokerName,
            String eventDate
    ) {}
}
