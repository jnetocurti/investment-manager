package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CreateSplitCorporateActionCommand {
    private final String ticker;
    private final AssetType assetType;
    private final BigDecimal factor;
    private final BigDecimal fee;
    private final LocalDate eventDate;
    private final String brokerName;
    private final String brokerDocument;
    private final String sourceReferenceId;
    private final String originalEventId;
}
