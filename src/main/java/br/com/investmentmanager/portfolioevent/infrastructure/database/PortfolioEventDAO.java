package br.com.investmentmanager.portfolioevent.infrastructure.database;

import br.com.investmentmanager.portfolioevent.infrastructure.database.model.PersistencePortfolioEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PortfolioEventDAO extends MongoRepository<PersistencePortfolioEvent, UUID> {
}
