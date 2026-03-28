package com.investmentmanager.portfolioevent.adapter.in.rest;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.model.PortfolioEvent;
import com.investmentmanager.portfolioevent.domain.port.in.CorporateActionUseCase;
import com.investmentmanager.portfolioevent.domain.port.in.CreateCorporateActionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/portfolio-events")
@RequiredArgsConstructor
public class CorporateActionController {

    private final CorporateActionUseCase corporateActionUseCase;

    @PostMapping("/split")
    public ResponseEntity<?> split(@RequestBody CorporateActionRequest request) {
        return create(request, true);
    }

    @PostMapping("/reverse-split")
    public ResponseEntity<?> reverseSplit(@RequestBody CorporateActionRequest request) {
        return create(request, false);
    }

    private ResponseEntity<?> create(CorporateActionRequest request, boolean split) {
        try {
            CreateCorporateActionCommand command = CreateCorporateActionCommand.builder()
                    .ticker(request.ticker())
                    .assetType(request.assetType() != null ? AssetType.valueOf(request.assetType()) : AssetType.STOCKS_BRL)
                    .brokerName(request.brokerName())
                    .brokerDocument(request.brokerDocument())
                    .eventDate(request.eventDate())
                    .ratioNumerator(request.ratio() != null ? request.ratio().numerator() : null)
                    .ratioDenominator(request.ratio() != null ? request.ratio().denominator() : null)
                    .build();

            PortfolioEvent result = split
                    ? corporateActionUseCase.createSplit(command)
                    : corporateActionUseCase.createReverseSplit(command);

            return ResponseEntity.ok(new CorporateActionResponse(
                    result.getId(),
                    result.getEventType().name(),
                    result.getAssetName(),
                    result.getEventDate(),
                    result.getRatio().getNumerator(),
                    result.getRatio().getDenominator()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    record CorporateActionRequest(
            String ticker,
            String assetType,
            String brokerName,
            String brokerDocument,
            LocalDate eventDate,
            RatioRequest ratio
    ) {}

    record RatioRequest(BigDecimal numerator, BigDecimal denominator) {}

    record CorporateActionResponse(
            String id,
            String eventType,
            String ticker,
            LocalDate eventDate,
            BigDecimal numerator,
            BigDecimal denominator
    ) {}
}
