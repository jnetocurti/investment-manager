package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CreateSplitCommand {

    String targetTicker;
    AssetType targetAssetType;
    String ratio;
    LocalDate eventDate;
    String brokerName;
    String brokerDocument;
    String currency;
}
