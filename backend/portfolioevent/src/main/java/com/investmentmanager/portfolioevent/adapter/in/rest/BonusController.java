package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.BonusUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateBonusCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/bonuses")
@RequiredArgsConstructor
public class BonusController {

    private final BonusUseCase bonusUseCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        try {
            var command = CreateBonusCommand.builder()
                    .targetTicker(request.targetTicker())
                    .targetAssetType(request.targetAssetType() != null
                            ? AssetType.valueOf(request.targetAssetType()) : null)
                    .ratio(request.ratio())
                    .unitPrice(request.unitPrice())
                    .eventDate(request.eventDate())
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .currency(request.currency())
                    .build();

            PortfolioEvent result = bonusUseCase.create(command);
            return ResponseEntity.ok(toResponse(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    private CreateResponse toResponse(PortfolioEvent event) {
        return new CreateResponse(
                event.getId(),
                event.getEventType().name(),
                event.getAssetName(),
                event.getAssetType() != null ? event.getAssetType().name() : null,
                event.getMetadata() != null ? event.getMetadata().getBonusRatio() : null,
                event.getMetadata() != null ? event.getMetadata().getBonusBaseQuantity() : null,
                event.getQuantity(),
                event.getUnitPrice().toString(),
                event.getTotalValue().toString(),
                event.getBrokerKey(),
                event.getEventDate().toString(),
                event.getSourceReferenceId());
    }

    record CreateRequest(
            String targetTicker,
            String targetAssetType,
            String ratio,
            BigDecimal unitPrice,
            LocalDate eventDate,
            String brokerName,
            String brokerDocument,
            String currency
    ) {
    }

    record CreateResponse(
            String id,
            String eventType,
            String targetTicker,
            String targetAssetType,
            String ratio,
            Integer baseQuantity,
            int bonusQuantity,
            String unitPrice,
            String totalValue,
            String brokerKey,
            String eventDate,
            String sourceReferenceId
    ) {
    }
}
