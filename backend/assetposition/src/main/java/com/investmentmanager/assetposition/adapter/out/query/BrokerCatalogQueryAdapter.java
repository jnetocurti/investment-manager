package com.investmentmanager.assetposition.adapter.out.query;

import com.investmentmanager.assetposition.domain.port.out.BrokerCatalogQueryPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class BrokerCatalogQueryAdapter implements BrokerCatalogQueryPort {

    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<BrokerDisplayData> findByBrokerKey(String brokerKey) {
        Query query = Query.query(Criteria.where("brokerKey").is(brokerKey));
        BrokerCatalogDocument doc = mongoTemplate.findOne(query, BrokerCatalogDocument.class, "broker_catalog");
        if (doc == null) {
            return Optional.empty();
        }
        return Optional.of(new BrokerDisplayData(doc.getCurrentName(), doc.getCurrentDocument()));
    }

    @Data
    @Document(collection = "broker_catalog")
    static class BrokerCatalogDocument {
        @Id
        private String id;
        private String brokerKey;
        private String currentName;
        private String currentDocument;
    }
}
