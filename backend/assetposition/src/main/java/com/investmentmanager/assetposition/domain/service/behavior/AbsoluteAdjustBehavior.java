package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.commons.domain.model.adjustment.AbsoluteAdjustmentPayload;
import com.investmentmanager.commons.domain.model.adjustment.AdjustmentType;
import org.springframework.stereotype.Component;

@Component
public class AbsoluteAdjustBehavior implements PositionImpactBehavior {

    @Override
    public BehaviorKey key() {
        return new BehaviorKey(PositionImpactType.ADJUST, AdjustmentType.ABSOLUTE);
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        if (!(impact.getAdjustmentPayload() instanceof AbsoluteAdjustmentPayload absolute)) {
            throw new IllegalArgumentException("Absolute adjustment payload is required");
        }

        int targetQuantity = absolute.getTargetQuantity();
        var targetAvgPrice = absolute.getTargetAveragePrice();
        var totalCost = targetAvgPrice.multiply(targetQuantity).add(impact.getFee());
        return PositionApplyResult.of(new PositionState(targetQuantity, targetAvgPrice, totalCost));
    }
}
