package br.com.investmentmanager.tradingnote.infrastructure.database;

import br.com.investmentmanager.tradingnote.infrastructure.database.model.PersistenceTradingNote;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

interface PersistenceTradingNoteDAO extends MongoRepository<PersistenceTradingNote, UUID> {
}
