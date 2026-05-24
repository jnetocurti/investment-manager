package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.model.RealizedSaleResult;

final class RealizedSaleResultDocumentMapper {

    private RealizedSaleResultDocumentMapper() {
    }

    static RealizedSaleResultDocument toDocument(RealizedSaleResult domain) {
        RealizedSaleResultDocument doc = new RealizedSaleResultDocument();
        doc.setAssetName(domain.getAssetName());
        doc.setAssetType(domain.getAssetType().name());
        doc.setBrokerKey(domain.getBrokerKey());
        doc.setEventDate(domain.getEventDate());
        doc.setEventOrder(domain.getEventOrder());
        doc.setSaleSourceType(domain.getSaleSourceType());
        doc.setSaleSourceReferenceId(domain.getSaleSourceReferenceId());
        doc.setSaleOriginalEventId(domain.getSaleOriginalEventId());
        doc.setSaleSequence(domain.getSaleSequence());
        doc.setQuantitySold(domain.getQuantitySold());
        doc.setAveragePriceUsed(domain.getAveragePriceUsed().toBigDecimal());
        doc.setUnitSalePrice(domain.getUnitSalePrice().toBigDecimal());
        doc.setGrossSaleAmount(domain.getGrossSaleAmount().toBigDecimal());
        doc.setAllocatedOperationalCost(domain.getAllocatedOperationalCost().toBigDecimal());
        doc.setNetSaleAmount(domain.getNetSaleAmount().toBigDecimal());
        doc.setCostBasisAmount(domain.getCostBasisAmount().toBigDecimal());
        doc.setRealizedResultAmount(domain.getRealizedResultAmount().toBigDecimal());
        doc.setResultType(domain.getResultType().name());
        doc.setCurrency(domain.getCurrency());
        doc.setRecordedAt(domain.getRecordedAt());
        doc.setSchemaVersion(domain.getSchemaVersion());
        return doc;
    }
}
