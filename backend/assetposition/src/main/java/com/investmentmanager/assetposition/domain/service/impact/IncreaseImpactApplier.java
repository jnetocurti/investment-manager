package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionImpactType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class IncreaseImpactApplier implements PositionImpactApplier {

    @Override
    public boolean supports(PositionImpactData impact) {
        return impact.getImpactType() == PositionImpactType.INCREASE;
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        int effectiveQuantity = resolveEffectiveQuantity(current, impact);
        MonetaryValue eventCost = impact.getUnitPrice().multiply(effectiveQuantity).add(impact.getFee());
        MonetaryValue totalCost = current.getTotalCost().add(eventCost);
        int quantity = current.getQuantity() + effectiveQuantity;
        MonetaryValue avgPrice = totalCost.divide(BigDecimal.valueOf(quantity));

        return PositionApplyResult.of(current.toBuilder()
                .quantity(quantity)
                .averagePrice(avgPrice)
                .totalCost(totalCost)
                .build());
    }

    private int resolveEffectiveQuantity(PositionState current, PositionImpactData impact) {
        if ("BONUS".equals(impact.getOriginType()) && impact.getFactor() != null) {
            if (current.getQuantity() <= 0) {
                throw new IllegalStateException("Bonificação sem posição base elegível na data do evento");
            }
            int calculatedQuantity = BigDecimal.valueOf(current.getQuantity())
                    .multiply(impact.getFactor())
                    .setScale(0, RoundingMode.DOWN)
                    .intValueExact();
            if (calculatedQuantity <= 0) {
                throw new IllegalStateException("Bonificação não gera quantidade elegível para a posição base na data");
            }
            return calculatedQuantity;
        }
        return impact.getQuantity();
    }
}
