package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CreateTickerRenameCommand {

    String oldTicker;
    String newTicker;
    AssetType assetType;
    LocalDate eventDate;
    String brokerName;
    String brokerDocument;
    String currency;
}
