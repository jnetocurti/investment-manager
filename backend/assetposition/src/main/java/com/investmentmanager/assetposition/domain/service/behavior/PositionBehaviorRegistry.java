package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.commons.domain.model.adjustment.AdjustmentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PositionBehaviorRegistry implements PositionBehaviorResolver {

    private final Map<BehaviorKey, PositionImpactBehavior> registry;

    public PositionBehaviorRegistry(List<PositionImpactBehavior> behaviors) {
        this.registry = behaviors.stream().collect(Collectors.toMap(PositionImpactBehavior::key, Function.identity()));
    }

    @Override
    public PositionImpactBehavior resolve(PositionImpactData impact) {
        AdjustmentType adjustmentType = impact.getImpactType() == PositionImpactType.ADJUST
                ? resolveAdjustmentType(impact)
                : null;

        BehaviorKey key = new BehaviorKey(impact.getImpactType(), adjustmentType);
        PositionImpactBehavior behavior = registry.get(key);
        if (behavior == null) {
            throw new IllegalStateException("No behavior found for key: " + key);
        }
        return behavior;
    }

    private AdjustmentType resolveAdjustmentType(PositionImpactData impact) {
        if (impact.getAdjustmentType() == null) {
            throw new IllegalStateException("ADJUST impact requires adjustmentType");
        }
        return impact.getAdjustmentType();
    }
}
