package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IncreaseBehavior implements PositionImpactBehavior {

    @Override
    public BehaviorKey key() {
        return new BehaviorKey(PositionImpactType.INCREASE, null);
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        var eventCost = impact.getUnitPrice().multiply(impact.getQuantity()).add(impact.getFee());
        var totalCost = current.totalCost().add(eventCost);
        var quantity = current.quantity() + impact.getQuantity();
        var avgPrice = totalCost.divide(BigDecimal.valueOf(quantity));
        return PositionApplyResult.of(new PositionState(quantity, avgPrice, totalCost));
    }
}
