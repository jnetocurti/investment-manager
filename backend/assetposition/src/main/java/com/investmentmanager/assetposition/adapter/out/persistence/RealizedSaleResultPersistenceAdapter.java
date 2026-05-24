package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.RealizedSaleResult;
import com.investmentmanager.assetposition.domain.port.out.RealizedSaleResultRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class RealizedSaleResultPersistenceAdapter implements RealizedSaleResultRepositoryPort {

    private final MongoOperations mongoOperations;

    @Override
    public void upsertAllByAssetAndBroker(String assetName, String brokerKey, List<RealizedSaleResult> results) {
        Query deleteQuery = Query.query(Criteria.where("assetName").is(assetName).and("brokerKey").is(brokerKey));
        mongoOperations.remove(deleteQuery, RealizedSaleResultDocument.class);

        if (results.isEmpty()) {
            return;
        }

        BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, RealizedSaleResultDocument.class);
        for (RealizedSaleResult result : results) {
            Query query = Query.query(Criteria.where("saleOriginalEventId").is(result.getSaleOriginalEventId())
                    .and("saleSequence").is(result.getSaleSequence())
                    .and("brokerKey").is(result.getBrokerKey())
                    .and("assetName").is(result.getAssetName()));
            RealizedSaleResultDocument doc = RealizedSaleResultDocumentMapper.toDocument(result);
            Update update = new Update()
                    .set("assetType", doc.getAssetType())
                    .set("eventDate", doc.getEventDate())
                    .set("eventOrder", doc.getEventOrder())
                    .set("saleSourceType", doc.getSaleSourceType())
                    .set("saleSourceReferenceId", doc.getSaleSourceReferenceId())
                    .set("quantitySold", doc.getQuantitySold())
                    .set("averagePriceUsed", doc.getAveragePriceUsed())
                    .set("unitSalePrice", doc.getUnitSalePrice())
                    .set("grossSaleAmount", doc.getGrossSaleAmount())
                    .set("allocatedOperationalCost", doc.getAllocatedOperationalCost())
                    .set("netSaleAmount", doc.getNetSaleAmount())
                    .set("costBasisAmount", doc.getCostBasisAmount())
                    .set("realizedResultAmount", doc.getRealizedResultAmount())
                    .set("resultType", doc.getResultType())
                    .set("currency", doc.getCurrency())
                    .set("recordedAt", doc.getRecordedAt())
                    .set("schemaVersion", doc.getSchemaVersion());
            bulkOps.upsert(query, update);
        }
        bulkOps.execute();
    }
}
