package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import org.springframework.stereotype.Component;

@Component
public class DecreaseBehavior implements PositionImpactBehavior {

    @Override
    public BehaviorKey key() {
        return new BehaviorKey(PositionImpactType.DECREASE, null);
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        int quantity = current.quantity() - impact.getQuantity();
        if (quantity <= 0) {
            return PositionApplyResult.of(PositionState.empty());
        }

        MonetaryValue avgPrice = current.averagePrice();
        MonetaryValue totalCost = avgPrice.multiply(quantity);
        return PositionApplyResult.of(new PositionState(quantity, avgPrice, totalCost));
    }
}
