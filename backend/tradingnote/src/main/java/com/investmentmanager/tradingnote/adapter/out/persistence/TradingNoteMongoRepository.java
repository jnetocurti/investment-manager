package com.investmentmanager.tradingnote.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TradingNoteMongoRepository extends MongoRepository<TradingNoteDocument, String> {

    boolean existsByFileHash(String fileHash);

    Optional<TradingNoteDocument> findByFileHash(String fileHash);
}
