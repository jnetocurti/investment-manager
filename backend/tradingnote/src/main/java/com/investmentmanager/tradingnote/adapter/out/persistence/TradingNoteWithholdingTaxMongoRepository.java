package com.investmentmanager.tradingnote.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TradingNoteWithholdingTaxMongoRepository extends MongoRepository<TradingNoteWithholdingTaxDocument, String> {
    Optional<TradingNoteWithholdingTaxDocument> findByTradingNoteIdAndTaxType(String tradingNoteId, String taxType);
}
