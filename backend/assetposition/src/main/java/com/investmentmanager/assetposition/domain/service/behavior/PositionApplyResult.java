package com.investmentmanager.assetposition.domain.service.behavior;

import java.util.List;

public record PositionApplyResult(
        PositionState newState,
        List<PositionDomainEffect> effects
) {
    public static PositionApplyResult of(PositionState state) {
        return new PositionApplyResult(state, List.of());
    }
}
