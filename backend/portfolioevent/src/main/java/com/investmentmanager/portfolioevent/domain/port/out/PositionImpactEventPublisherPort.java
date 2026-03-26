package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.util.List;

public interface PositionImpactEventPublisherPort {

    void publishAll(List<PositionImpactEvent> impacts);
}
