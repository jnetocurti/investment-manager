package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CreateCorporateActionCommand {
    private final String ticker;
    private final AssetType assetType;
    private final String brokerName;
    private final String brokerDocument;
    private final LocalDate eventDate;
    private final BigDecimal ratioNumerator;
    private final BigDecimal ratioDenominator;
}
