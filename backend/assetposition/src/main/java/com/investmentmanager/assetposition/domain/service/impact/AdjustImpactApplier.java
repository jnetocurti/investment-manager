package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.PositionAdjustmentType;
import com.investmentmanager.commons.domain.model.PositionImpactType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AdjustImpactApplier implements PositionImpactApplier {

    @Override
    public boolean supports(PositionImpactData impact) {
        return impact.getImpactType() == PositionImpactType.ADJUST;
    }

    @Override
    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        if (isSplitAdjustment(impact)) {
            return applySplit(current, impact);
        }

        return applyGenericAdjust(current, impact);
    }

    private boolean isSplitAdjustment(PositionImpactData impact) {
        return impact.getAdjustmentType() == PositionAdjustmentType.SPLIT
                || (impact.getFactor() != null && impact.getFactor().compareTo(BigDecimal.ZERO) > 0);
    }

    private PositionApplyResult applySplit(PositionState current, PositionImpactData impact) {
        BigDecimal factor = impact.getFactor();
        BigDecimal splitQuantity = BigDecimal.valueOf(current.getQuantity()).multiply(factor);
        int quantity = splitQuantity.setScale(0, RoundingMode.DOWN).intValueExact();
        BigDecimal splitFractionQuantity = splitQuantity.subtract(BigDecimal.valueOf(quantity));
        MonetaryValue theoreticalPostSplitPrice = current.getAveragePrice().divide(factor);
        MonetaryValue splitFractionResidualBookValue = theoreticalPostSplitPrice.multiply(splitFractionQuantity);
        MonetaryValue totalCost = current.getTotalCost()
                .subtract(splitFractionResidualBookValue)
                .add(impact.getFee());
        MonetaryValue averagePrice = quantity > 0
                ? totalCost.divide(BigDecimal.valueOf(quantity))
                : MonetaryValue.zero();

        validateSplitBookIdentity(current, impact, totalCost, splitFractionResidualBookValue);

        return PositionApplyResult.of(current.toBuilder()
                .quantity(quantity)
                .averagePrice(averagePrice)
                .totalCost(totalCost)
                .build());
    }

    private void validateSplitBookIdentity(PositionState current,
                                           PositionImpactData impact,
                                           MonetaryValue adjustedTotalCost,
                                           MonetaryValue splitFractionResidualBookValue) {
        MonetaryValue beforeSplit = current.getTotalCost().add(impact.getFee());
        MonetaryValue recomposed = adjustedTotalCost.add(splitFractionResidualBookValue);
        if (!beforeSplit.equals(recomposed)) {
            throw new IllegalStateException("Inconsistência contábil no split: total antes diferente da recomposição");
        }
    }

    private PositionApplyResult applyGenericAdjust(PositionState current, PositionImpactData impact) {
        int quantity = impact.getQuantity();
        MonetaryValue averagePrice = impact.getUnitPrice();
        MonetaryValue totalCost = averagePrice.multiply(quantity).add(impact.getFee());

        return PositionApplyResult.of(current.toBuilder()
                .quantity(quantity)
                .averagePrice(averagePrice)
                .totalCost(totalCost)
                .build());
    }
}
