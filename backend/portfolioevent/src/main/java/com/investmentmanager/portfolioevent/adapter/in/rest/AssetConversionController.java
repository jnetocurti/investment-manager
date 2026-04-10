package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.AssetConversionUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateAssetConversionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/asset-conversions")
@RequiredArgsConstructor
public class AssetConversionController {

    private final AssetConversionUseCase assetConversionUseCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        try {
            var command = CreateAssetConversionCommand.builder()
                    .oldTicker(request.oldTicker())
                    .newTicker(request.newTicker())
                    .assetType(request.assetType() != null ? AssetType.valueOf(request.assetType()) : null)
                    .ratio(request.ratio())
                    .eventDate(request.eventDate())
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .currency(request.currency())
                    .build();

            PortfolioEvent result = assetConversionUseCase.create(command);
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
                event.getMetadata() != null ? event.getMetadata().getSplitRatio() : null,
                event.getBrokerKey(),
                event.getEventDate().toString(),
                event.getSourceReferenceId());
    }

    record CreateRequest(
            String oldTicker,
            String newTicker,
            String assetType,
            String ratio,
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
            String ratio,
            String brokerKey,
            String eventDate,
            String sourceReferenceId
    ) {
    }
}
