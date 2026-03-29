package com.investmentmanager.assetposition.domain.service.impact;

import com.investmentmanager.assetposition.domain.model.PositionImpactData;

import java.util.List;

public class PositionImpactApplierRegistry {

    private final List<PositionImpactApplier> appliers;

    public PositionImpactApplierRegistry(List<PositionImpactApplier> appliers) {
        this.appliers = List.copyOf(appliers);
    }

    public PositionApplyResult apply(PositionState current, PositionImpactData impact) {
        return appliers.stream()
                .filter(applier -> applier.supports(impact))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No applier found for impact type " + impact.getImpactType()))
                .apply(current, impact);
    }

    public static PositionImpactApplierRegistry defaultRegistry() {
        return new PositionImpactApplierRegistry(List.of(
                new IncreaseImpactApplier(),
                new DecreaseImpactApplier(),
                new AdjustImpactApplier()
        ));
    }
}
