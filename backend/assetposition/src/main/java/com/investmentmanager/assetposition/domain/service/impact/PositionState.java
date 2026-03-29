package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PositionState {
    private final int quantity;
    private final MonetaryValue averagePrice;
    private final MonetaryValue totalCost;
}
