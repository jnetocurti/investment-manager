package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.commons.domain.model.MonetaryValue;

public record PositionState(
        int quantity,
        MonetaryValue averagePrice,
        MonetaryValue totalCost
) {
    public static PositionState empty() {
        return new PositionState(0, MonetaryValue.zero(), MonetaryValue.zero());
    }
}
