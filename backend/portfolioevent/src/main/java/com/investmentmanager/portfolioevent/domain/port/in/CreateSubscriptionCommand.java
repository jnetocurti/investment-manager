package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CreateSubscriptionCommand {

    String subscriptionTicker;
    String targetTicker;
    AssetType targetAssetType;
    int quantity;
    BigDecimal unitPrice;
    BigDecimal totalValue;
    BigDecimal fee;
    String currency;
    String brokerName;
    String brokerDocument;
    LocalDate subscriptionDate;
    LocalDate conversionDate; // opcional — se informado, já gera conversão
}
