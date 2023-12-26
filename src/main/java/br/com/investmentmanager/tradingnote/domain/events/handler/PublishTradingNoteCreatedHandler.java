package br.com.investmentmanager.tradingnote.domain.events.handler;

import br.com.investmentmanager.shared.domain.events.handler.EventHandler;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;

public interface PublishTradingNoteCreatedHandler extends EventHandler<TradingNoteCreated> {
}
