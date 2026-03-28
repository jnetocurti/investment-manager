package com.investmentmanager.assetposition.domain.service.behavior;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;

public interface PositionImpactBehavior {

    BehaviorKey key();

    PositionApplyResult apply(PositionState current, PositionImpactData impact);
}
