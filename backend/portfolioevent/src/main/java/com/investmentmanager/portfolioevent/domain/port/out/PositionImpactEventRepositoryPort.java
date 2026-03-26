package com.investmentmanager.portfolioevent.domain.port.out;

import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;

import java.util.List;

public interface PositionImpactEventRepositoryPort {

    List<PositionImpactEvent> saveAll(List<PositionImpactEvent> impacts);

    boolean existsByUniqueKey(String originalEventId, String impactType, int sequence);
}
