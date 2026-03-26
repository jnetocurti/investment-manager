package com.investmentmanager.tradingnote.adapter.out.persistence;

import com.investmentmanager.commons.domain.model.Broker;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.OperationType;
import com.investmentmanager.tradingnote.domain.model.Fee;
import com.investmentmanager.tradingnote.domain.model.Operation;
import com.investmentmanager.tradingnote.domain.model.TradingNote;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TradingNoteDocumentMapper {

    static TradingNoteDocument toDocument(TradingNote note) {
        var doc = new TradingNoteDocument();
        doc.setFileHash(note.getFileHash());
        doc.setNoteNumber(note.getNoteNumber());
        doc.setBrokerName(note.getBroker().getName());
        doc.setBrokerDocumentId(note.getBroker().getDocumentId());
        doc.setTradingDate(note.getTradingDate());
        doc.setSettlementDate(note.getSettlementDate());
        doc.setTotalNote(note.getTotalNote().toDisplayValue());
        doc.setNetOperations(note.getNetOperations().toDisplayValue());
        doc.setTotalFees(note.getTotalFees().toDisplayValue());
        doc.setFileReference(note.getFileReference());
        doc.setCurrency(note.getCurrency());

        doc.setOperations(note.getOperations().stream()
                .map(op -> new TradingNoteDocument.OperationDoc(
                        op.getAssetDescription(), op.getType().name(), op.getQuantity(),
                        op.getUnitPrice().toDisplayValue(), op.getTotalValue().toDisplayValue(),
                        op.getFee().toDisplayValue()))
                .toList());

        doc.setFees(note.getFees().stream()
                .map(f -> new TradingNoteDocument.FeeDoc(f.getDescription(), f.getValue().toDisplayValue()))
                .toList());

        return doc;
    }

    static TradingNote toDomain(TradingNoteDocument doc) {
        var operations = doc.getOperations().stream()
                .map(op -> Operation.builder()
                        .assetDescription(op.assetDescription())
                        .type(OperationType.valueOf(op.type()))
                        .quantity(op.quantity())
                        .unitPrice(MonetaryValue.of(op.unitPrice()))
                        .totalValue(MonetaryValue.of(op.totalValue()))
                        .fee(MonetaryValue.of(op.fee()))
                        .build())
                .toList();

        var fees = doc.getFees().stream()
                .map(f -> new Fee(f.description(), MonetaryValue.of(f.value())))
                .toList();

        return TradingNote.builder()
                .id(doc.getId())
                .noteNumber(doc.getNoteNumber())
                .broker(new Broker(doc.getBrokerName(), doc.getBrokerDocumentId()))
                .tradingDate(doc.getTradingDate())
                .settlementDate(doc.getSettlementDate())
                .operations(operations)
                .fees(fees)
                .totalNote(MonetaryValue.of(doc.getTotalNote()))
                .netOperations(MonetaryValue.of(doc.getNetOperations()))
                .totalFees(MonetaryValue.of(doc.getTotalFees()))
                .fileReference(doc.getFileReference())
                .fileHash(doc.getFileHash())
                .currency(doc.getCurrency())
                .build();
    }
}
