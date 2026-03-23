package com.investmentmanager.tradingnote.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingNoteCreatedEvent {

    private String tradingNoteId;
    private String noteNumber;
    private String brokerName;
    private LocalDate tradingDate;
    private List<OperationEvent> operations;
    private BigDecimal totalNote;
    private BigDecimal totalFees;
    private String currency;

    public static TradingNoteCreatedEvent from(TradingNote note) {
        return TradingNoteCreatedEvent.builder()
                .tradingNoteId(note.getId())
                .noteNumber(note.getNoteNumber())
                .brokerName(note.getBroker().getName())
                .tradingDate(note.getTradingDate())
                .totalNote(note.getTotalNote().toDisplayValue())
                .totalFees(note.getTotalFees().toDisplayValue())
                .currency(note.getCurrency())
                .operations(note.getOperations().stream()
                        .map(op -> new OperationEvent(
                                op.getAssetName(),
                                op.getType().name(),
                                op.getQuantity(),
                                op.getUnitPrice().toDisplayValue(),
                                op.getTotalValue().toDisplayValue(),
                                op.getFee().toDisplayValue()))
                        .toList())
                .build();
    }

    public record OperationEvent(
            String assetName,
            String operationType,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalValue,
            BigDecimal fee
    ) {}
}
