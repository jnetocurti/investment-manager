package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CreateGenericCorporateActionCommand;
import com.investmentmanager.portfolioevent.domain.port.in.GenericCorporateActionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/generic-corporate-actions")
@RequiredArgsConstructor
public class GenericCorporateActionController {

    private final GenericCorporateActionUseCase useCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        try {
            PortfolioEvent created = useCase.create(CreateGenericCorporateActionCommand.builder()
                    .eventDate(request.eventDate())
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .currency(request.currency())
                    .observation(request.observation())
                    .targetAsset(toAsset(request.targetAsset()))
                    .derivedAssets(request.derivedAssets() == null ? List.of() : request.derivedAssets().stream().map(this::toAsset).toList())
                    .residual(request.residual() == null ? null : CreateGenericCorporateActionCommand.Residual.builder()
                            .amount(request.residual().amount())
                            .description(request.residual().description())
                            .build())
                    .build());
            return ResponseEntity.ok(new CreateResponse(
                    created.getId(),
                    created.getEventType().name(),
                    created.getAssetName(),
                    created.getAssetType() != null ? created.getAssetType().name() : null,
                    created.getBrokerKey(),
                    created.getEventDate(),
                    created.getSourceReferenceId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    private CreateGenericCorporateActionCommand.AssetTarget toAsset(AssetPayload payload) {
        if (payload == null) return null;
        return CreateGenericCorporateActionCommand.AssetTarget.builder()
                .ticker(payload.ticker())
                .assetType(payload.assetType() != null ? AssetType.valueOf(payload.assetType()) : null)
                .quantity(payload.quantity())
                .averagePrice(payload.averagePrice())
                .build();
    }

    record CreateRequest(LocalDate eventDate, String brokerName, String brokerDocument, String currency,
                         String observation, AssetPayload targetAsset, List<AssetPayload> derivedAssets,
                         ResidualPayload residual) {}

    record AssetPayload(String ticker, String assetType, Integer quantity, java.math.BigDecimal averagePrice) {}

    record ResidualPayload(java.math.BigDecimal amount, String description) {}

    record CreateResponse(
            String id,
            String eventType,
            String targetTicker,
            String targetAssetType,
            String brokerKey,
            LocalDate eventDate,
            String sourceReferenceId
    ) {}
}
