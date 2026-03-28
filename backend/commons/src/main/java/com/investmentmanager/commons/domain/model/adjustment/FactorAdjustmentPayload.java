package com.investmentmanager.commons.domain.model.adjustment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FactorAdjustmentPayload implements AdjustmentPayload {

    private final Ratio ratio;

    @Override
    public AdjustmentType getType() {
        return AdjustmentType.FACTOR;
    }
}
