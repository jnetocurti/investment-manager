package com.investmentmanager.tradingnote.adapter.out.parser;

import java.util.List;

record RawNoteData(
        String brokerName,
        String brokerDocumentId,
        String noteNumber,
        String tradingDate,
        String settlementDate,
        String totalNote,
        String currency,
        List<RawFee> fees,
        List<RawOperation> operations
) {
    record RawFee(String description, String value) {}

    record RawOperation(
            String assetDescription,
            String operationType,
            int quantity,
            String unitPrice,
            String totalValue
    ) {}
}
