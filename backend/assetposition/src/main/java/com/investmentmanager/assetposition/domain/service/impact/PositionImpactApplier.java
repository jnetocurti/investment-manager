package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;

public interface PositionImpactApplier {

    boolean supports(PositionImpactData impact);

    PositionApplyResult apply(PositionState current, PositionImpactData impact);
}
