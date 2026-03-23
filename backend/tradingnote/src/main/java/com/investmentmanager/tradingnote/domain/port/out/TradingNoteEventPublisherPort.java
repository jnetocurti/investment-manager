package com.investmentmanager.tradingnote.domain.port.out;

import com.investmentmanager.tradingnote.domain.model.TradingNoteCreatedEvent;

public interface TradingNoteEventPublisherPort {

    void publishCreated(TradingNoteCreatedEvent event);
}
