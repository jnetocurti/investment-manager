package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.commons.domain.model.PositionImpactType;
import com.investmentmanager.commons.domain.model.adjustment.AdjustmentType;

public record BehaviorKey(
        PositionImpactType impactType,
        AdjustmentType adjustmentType
) {
}
