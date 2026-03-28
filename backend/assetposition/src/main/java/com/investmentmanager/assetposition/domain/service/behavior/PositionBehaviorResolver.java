package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;

public interface PositionBehaviorResolver {
    PositionImpactBehavior resolve(PositionImpactData impact);
}
