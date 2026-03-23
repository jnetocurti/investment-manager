package com.investmentmanager.asset.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    private String id;
    private String ticker;
    private int totalQuantity;
    private BigDecimal averagePrice;
    private BigDecimal totalInvested;
}
