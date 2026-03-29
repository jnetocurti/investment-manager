package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;

public class DecreaseImpactApplier implements PositionImpactApplier {

    @Override
    public boolean supports(PositionImpactData impact) {
        return impact.getImpactType() == PositionImpactType.DECREASE;
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        int quantity = current.getQuantity() - impact.getQuantity();
        MonetaryValue averagePrice = current.getAveragePrice();
        MonetaryValue totalCost = current.getTotalCost();

        if (quantity <= 0) {
            quantity = 0;
            averagePrice = MonetaryValue.zero();
            totalCost = MonetaryValue.zero();
        } else {
            totalCost = averagePrice.multiply(quantity);
        }

        return PositionApplyResult.of(current.toBuilder()
                .quantity(quantity)
                .averagePrice(averagePrice)
                .totalCost(totalCost)
                .build());
    }
}
