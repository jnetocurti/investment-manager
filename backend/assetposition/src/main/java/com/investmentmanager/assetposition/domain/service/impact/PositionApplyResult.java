package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PositionApplyResult {

    private final PositionState state;
    private final MonetaryValue splitFractionResidualBookValue;

    public static PositionApplyResult of(PositionState state) {
        return PositionApplyResult.builder().state(state).build();
    }

    public boolean hasSplitFractionResidualBookValue() {
        return splitFractionResidualBookValue != null && !splitFractionResidualBookValue.isZero();
    }
}
