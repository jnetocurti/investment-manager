package com.investmentmanager.commons.domain.model.adjustment;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AbsoluteAdjustmentPayload implements AdjustmentPayload {

    private final int targetQuantity;
    private final MonetaryValue targetAveragePrice;

    @Override
    public AdjustmentType getType() {
        return AdjustmentType.ABSOLUTE;
    }
}
