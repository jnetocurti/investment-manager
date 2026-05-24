package com.investmentmanager.assetposition.domain.model;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class RealizedSaleResult {

    private final String assetName;
    private final AssetType assetType;
    private final String brokerKey;
    private final LocalDate eventDate;
    private final Integer eventOrder;
    private final String saleSourceType;
    private final String saleSourceReferenceId;
    private final String saleOriginalEventId;
    private final int saleSequence;
    private final int quantitySold;
    private final MonetaryValue averagePriceUsed;
    private final MonetaryValue unitSalePrice;
    private final MonetaryValue grossSaleAmount;
    private final MonetaryValue allocatedOperationalCost;
    private final MonetaryValue netSaleAmount;
    private final MonetaryValue costBasisAmount;
    private final MonetaryValue realizedResultAmount;
    private final RealizedResultType resultType;
    private final String currency;
    private final LocalDateTime recordedAt;
    private final int schemaVersion;
}
