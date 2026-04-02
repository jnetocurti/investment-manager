package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.commons.domain.model.MonetaryValue;

public interface SplitFractionMetadataPort {

    void updateSplitFractionMetadata(String splitEventId,
                                     MonetaryValue splitFractionResidualBookValue,
                                     String splitFractionSourceReferenceId);
}
