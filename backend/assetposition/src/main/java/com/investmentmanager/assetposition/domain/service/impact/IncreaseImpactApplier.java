package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;

import java.math.BigDecimal;

public class IncreaseImpactApplier implements PositionImpactApplier {

    @Override
    public boolean supports(PositionImpactData impact) {
        return impact.getImpactType() == PositionImpactType.INCREASE;
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        MonetaryValue eventCost = impact.getUnitPrice().multiply(impact.getQuantity()).add(impact.getFee());
        MonetaryValue totalCost = current.getTotalCost().add(eventCost);
        int quantity = current.getQuantity() + impact.getQuantity();
        MonetaryValue avgPrice = totalCost.divide(BigDecimal.valueOf(quantity));

        return PositionApplyResult.of(current.toBuilder()
                .quantity(quantity)
                .averagePrice(avgPrice)
                .totalCost(totalCost)
                .build());
    }
}
