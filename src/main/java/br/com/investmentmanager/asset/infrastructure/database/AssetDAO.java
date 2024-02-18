package br.com.investmentmanager.asset.infrastructure.database;

import br.com.investmentmanager.asset.infrastructure.database.model.PersistenceAsset;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AssetDAO extends MongoRepository<PersistenceAsset, UUID> {
}
