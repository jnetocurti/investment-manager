package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateSplitCommand;
import com.investmentmanager.portfolioevent.domain.port.in.SplitUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/splits")
@RequiredArgsConstructor
public class SplitController {

    private final SplitUseCase splitUseCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        try {
            var command = CreateSplitCommand.builder()
                    .targetTicker(request.targetTicker())
                    .targetAssetType(request.targetAssetType() != null
                            ? AssetType.valueOf(request.targetAssetType()) : null)
                    .ratio(request.ratio())
                    .eventDate(request.eventDate())
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .currency(request.currency())
                    .build();

            PortfolioEvent result = splitUseCase.create(command);
            return ResponseEntity.ok(toResponse(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    private SplitResponse toResponse(PortfolioEvent event) {
        return new SplitResponse(
                event.getId(),
                event.getEventType().name(),
                event.getAssetName(),
                event.getAssetType() != null ? event.getAssetType().name() : null,
                event.getMetadata() != null ? event.getMetadata().getSplitRatio() : null,
                event.getBrokerKey(),
                event.getEventDate().toString());
    }

    record CreateRequest(
            String targetTicker,
            String targetAssetType,
            String ratio,
            LocalDate eventDate,
            String brokerName,
            String brokerDocument,
            String currency
    ) {
    }

    record SplitResponse(
            String id,
            String eventType,
            String targetTicker,
            String targetAssetType,
            String ratio,
            String brokerKey,
            String eventDate
    ) {
    }
}
