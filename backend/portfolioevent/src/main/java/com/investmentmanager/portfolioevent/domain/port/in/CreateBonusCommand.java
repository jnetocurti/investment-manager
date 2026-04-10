package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CreateBonusCommand {

    String targetTicker;
    AssetType targetAssetType;
    String ratio;
    BigDecimal unitPrice;
    LocalDate eventDate;
    String brokerName;
    String brokerDocument;
    String currency;
}
