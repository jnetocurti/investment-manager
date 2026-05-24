package com.investmentmanager.assetposition.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

interface RealizedSaleResultMongoRepository extends MongoRepository<RealizedSaleResultDocument, String> {
}
