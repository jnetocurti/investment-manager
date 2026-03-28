package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.commons.domain.model.adjustment.AdjustmentType;
import com.investmentmanager.commons.domain.model.adjustment.FactorAdjustmentPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FactorAdjustBehavior implements PositionImpactBehavior {

    @Override
    public BehaviorKey key() {
        return new BehaviorKey(PositionImpactType.ADJUST, AdjustmentType.FACTOR);
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        if (!(impact.getAdjustmentPayload() instanceof FactorAdjustmentPayload factorPayload)
                || factorPayload.getRatio() == null) {
            throw new IllegalArgumentException("Factor adjustment requires factor payload");
        }

        BigDecimal factor = factorPayload.getRatio().toFactor();
        if (factor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Factor adjustment requires factor > 0");
        }

        int quantity = BigDecimal.valueOf(current.quantity()).multiply(factor).intValue();
        var avgPrice = current.averagePrice().divide(factor);
        var totalCost = avgPrice.multiply(quantity).add(impact.getFee());
        return PositionApplyResult.of(new PositionState(quantity, avgPrice, totalCost));
    }
}
