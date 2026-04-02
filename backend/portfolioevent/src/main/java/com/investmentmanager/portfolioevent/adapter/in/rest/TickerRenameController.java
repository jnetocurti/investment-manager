package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateTickerRenameCommand;
import com.investmentmanager.portfolioevent.domain.port.in.TickerRenameUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ticker-renames")
@RequiredArgsConstructor
public class TickerRenameController {

    private final TickerRenameUseCase tickerRenameUseCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        try {
            var command = CreateTickerRenameCommand.builder()
                    .oldTicker(request.oldTicker())
                    .newTicker(request.newTicker())
                    .assetType(request.assetType() != null ? AssetType.valueOf(request.assetType()) : null)
                    .eventDate(request.eventDate())
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .currency(request.currency())
                    .build();

            PortfolioEvent result = tickerRenameUseCase.create(command);
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
                event.getMetadata() != null ? event.getMetadata().getOldTicker() : null,
                event.getMetadata() != null ? event.getMetadata().getNewTicker() : null,
                event.getAssetType() != null ? event.getAssetType().name() : null,
                event.getBrokerKey(),
                event.getEventDate().toString(),
                event.getSourceReferenceId());
    }

    record CreateRequest(
            String oldTicker,
            String newTicker,
            String assetType,
            LocalDate eventDate,
            String brokerName,
            String brokerDocument,
            String currency
    ) {
    }

    record CreateResponse(
            String id,
            String eventType,
            String oldTicker,
            String newTicker,
            String assetType,
            String brokerKey,
            String eventDate,
            String sourceReferenceId
    ) {
    }
}
