package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CorporateActionUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSplitCorporateActionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/corporate-actions")
@RequiredArgsConstructor
public class CorporateActionController {

    private final CorporateActionUseCase corporateActionUseCase;

    @PostMapping("/splits")
    public ResponseEntity<?> createSplit(@RequestBody CreateSplitRequest request) {
        try {
            CreateSplitCorporateActionCommand command = CreateSplitCorporateActionCommand.builder()
                    .ticker(request.ticker())
                    .assetType(request.assetType() != null ? AssetType.valueOf(request.assetType()) : null)
                    .factor(request.factor())
                    .fee(request.fee())
                    .eventDate(request.eventDate())
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .sourceReferenceId(request.sourceReferenceId())
                    .originalEventId(request.originalEventId())
                    .build();

            PositionImpactEvent event = corporateActionUseCase.createSplit(command);
            return ResponseEntity.ok(new SplitResponse(
                    event.getId(),
                    event.getOriginalEventId(),
                    event.getTicker(),
                    event.getImpactType().name(),
                    event.getAdjustmentType() != null ? event.getAdjustmentType().name() : null,
                    event.getFactor(),
                    event.getEventDate(),
                    event.getBrokerName(),
                    event.getBrokerDocument(),
                    event.getSourceReferenceId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    record CreateSplitRequest(
            String ticker,
            String assetType,
            BigDecimal factor,
            BigDecimal fee,
            LocalDate eventDate,
            String brokerName,
            String brokerDocument,
            String sourceReferenceId,
            String originalEventId
    ) {}

    record SplitResponse(
            String id,
            String originalEventId,
            String ticker,
            String impactType,
            String adjustmentType,
            BigDecimal factor,
            LocalDate eventDate,
            String brokerName,
            String brokerDocument,
            String sourceReferenceId
    ) {}
}
