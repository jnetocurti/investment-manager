package com.investmentmanager.assetposition.domain.service.impact;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PositionApplyResult {

    private final PositionState state;

    public static PositionApplyResult of(PositionState state) {
        return PositionApplyResult.builder().state(state).build();
    }
}
