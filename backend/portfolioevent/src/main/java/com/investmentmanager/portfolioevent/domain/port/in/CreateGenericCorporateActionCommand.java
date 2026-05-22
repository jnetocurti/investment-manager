package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class CreateGenericCorporateActionCommand {
    LocalDate eventDate;
    String brokerName;
    String brokerDocument;
    String currency;
    String observation;
    AssetTarget targetAsset;
    List<AssetTarget> derivedAssets;
    Residual residual;

    @Value
    @Builder
    public static class AssetTarget {
        String ticker;
        AssetType assetType;
        Integer quantity;
        BigDecimal averagePrice;
    }

    @Value
    @Builder
    public static class Residual {
        BigDecimal amount;
        String description;
    }
}
