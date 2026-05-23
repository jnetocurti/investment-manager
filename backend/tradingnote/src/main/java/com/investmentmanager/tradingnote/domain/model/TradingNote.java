package com.investmentmanager.tradingnote.domain.model;

import com.investmentmanager.commons.domain.model.Broker;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.OperationType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Getter
public class TradingNote {

    private static final String IRRF_NORMALIZED = "IRRF";

    private final String id;
    private final String noteNumber;
    private final Broker broker;
    private final LocalDate tradingDate;
    private final LocalDate settlementDate;
    private final List<Operation> operations;
    private final List<Fee> fees;
    private final MonetaryValue totalNote;
    private final MonetaryValue netOperations;
    private final MonetaryValue totalFees;
    private final MonetaryValue withholdingTaxesTotal;
    private final String fileReference;
    private final String fileHash;
    private final String currency;

    @Builder(toBuilder = true)
    private TradingNote(String id, String noteNumber, Broker broker,
                        LocalDate tradingDate, LocalDate settlementDate,
                        List<Operation> operations, List<Fee> fees,
                        MonetaryValue totalNote, MonetaryValue netOperations,
                        MonetaryValue totalFees, MonetaryValue withholdingTaxesTotal,
                        String fileReference, String fileHash,
                        String currency) {
        this.id = id;
        this.noteNumber = noteNumber;
        this.broker = broker;
        this.tradingDate = tradingDate;
        this.settlementDate = settlementDate;
        this.fees = fees;
        this.fileReference = fileReference;
        this.fileHash = fileHash;
        this.currency = currency != null ? currency : "BRL";
        this.totalNote = totalNote;

        if (netOperations != null) {
            this.netOperations = netOperations;
            this.totalFees = totalFees;
            this.withholdingTaxesTotal = withholdingTaxesTotal != null ? withholdingTaxesTotal : MonetaryValue.zero();
            this.operations = operations;
        } else {
            this.netOperations = operations.stream()
                    .map(Operation::getTotalValue)
                    .reduce(MonetaryValue.zero(), MonetaryValue::add);

            MonetaryValue totalExtractedFees = fees != null && !fees.isEmpty()
                    ? fees.stream().map(Fee::getValue).reduce(MonetaryValue.zero(), MonetaryValue::add)
                    : this.totalNote.subtract(this.netOperations).abs();

            this.withholdingTaxesTotal = fees == null
                    ? MonetaryValue.zero()
                    : fees.stream()
                    .filter(f -> isWithholdingTaxFee(f.getDescription()))
                    .map(Fee::getValue)
                    .reduce(MonetaryValue.zero(), MonetaryValue::add);

            this.totalFees = totalExtractedFees.subtract(this.withholdingTaxesTotal);
            this.operations = apportionFees(operations, this.totalFees);
        }

        validate();
    }

    public TradingNote withFileHash(String fileHash) {
        return this.toBuilder().fileHash(fileHash).build();
    }

    public TradingNote withFileReference(String fileReference) {
        return this.toBuilder().fileReference(fileReference).build();
    }

    public String buildStorageFilename() {
        String brokerName = broker.getName().toUpperCase();
        String date = tradingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return brokerName + "_" + date + "_" + noteNumber + ".pdf";
    }

    private void validate() {
        if (noteNumber == null || noteNumber.isBlank())
            throw new TradingNoteValidationException("Note number is required");
        if (broker == null)
            throw new TradingNoteValidationException("Broker is required");
        if (tradingDate == null)
            throw new TradingNoteValidationException("Trading date is required");
        if (operations == null || operations.isEmpty())
            throw new TradingNoteValidationException("At least one operation is required");
        if (totalNote == null || totalNote.isZero())
            throw new TradingNoteValidationException("Total note value is required");
        validateConsistency();
    }

    private void validateConsistency() {
        MonetaryValue sumApportioned = operations.stream()
                .map(Operation::getFee)
                .reduce(MonetaryValue.zero(), MonetaryValue::add);
        MonetaryValue diffApportioned = totalFees.subtract(sumApportioned).abs();
        if (diffApportioned.toBigDecimal().doubleValue() > 0.02) {
            throw new TradingNoteValidationException(
                    String.format("Inconsistent fee apportionment: totalFees=%s, sumApportionedFees=%s, diff=%s",
                            totalFees, sumApportioned, diffApportioned));
        }

        MonetaryValue signedNet = MonetaryValue.zero();
        for (Operation op : operations) {
            if (op.getType() == OperationType.SELL) {
                signedNet = signedNet.add(op.getTotalValue());
            } else {
                signedNet = signedNet.subtract(op.getTotalValue());
            }
        }
        MonetaryValue expectedLiquid = signedNet.subtract(totalFees).subtract(withholdingTaxesTotal);
        MonetaryValue diffLiquid = totalNote.abs().subtract(expectedLiquid.abs()).abs();
        double tolerance = Math.max(totalNote.abs().toBigDecimal().doubleValue() * 0.005, 1.00);
        if (diffLiquid.toBigDecimal().doubleValue() > tolerance) {
            throw new TradingNoteValidationException(
                    String.format("Inconsistent liquid value: totalNote=%s, expected=%s, diff=%s, tolerance=%.2f",
                            totalNote, expectedLiquid, diffLiquid, tolerance));
        }
    }

    private static boolean isWithholdingTaxFee(String description) {
        if (description == null) return false;
        String normalized = description.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        return normalized.contains(IRRF_NORMALIZED);
    }

    private static List<Operation> apportionFees(List<Operation> operations, MonetaryValue totalFees) {
        if (totalFees.isZero()) return operations;

        MonetaryValue grossVolume = operations.stream()
                .map(op -> op.getTotalValue().abs())
                .reduce(MonetaryValue.zero(), MonetaryValue::add);

        if (grossVolume.isZero()) return operations;

        BigDecimal factor = totalFees.toBigDecimal()
                .divide(grossVolume.toBigDecimal(), 10, RoundingMode.HALF_UP);
        for (Operation op : operations) {
            op.setFee(op.getTotalValue().abs().multiply(factor));
        }
        return operations;
    }
}
