package com.investmentmanager.portfolioevent.domain.port.in;

import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

public interface CorporateActionUseCase {

    PositionImpactEvent createSplit(CreateSplitCorporateActionCommand command);
}
