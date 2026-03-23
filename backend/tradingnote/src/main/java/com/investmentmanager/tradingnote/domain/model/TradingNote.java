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

/**
 * Aggregate root que representa uma nota de corretagem.
 *
 * <p>Duas formas de construção:</p>
 * <ul>
 *   <li><b>Parse do PDF (netOperations = null):</b> calcula netOperations, totalFees e rateia taxas nas operações.</li>
 *   <li><b>Reconstituição do banco (netOperations != null):</b> usa os valores já persistidos sem recalcular.</li>
 * </ul>
 */
@Getter
public class TradingNote {

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
    private final String fileReference;
    private final String fileHash;
    private final String currency;

    @Builder(toBuilder = true)
    private TradingNote(String id, String noteNumber, Broker broker,
                        LocalDate tradingDate, LocalDate settlementDate,
                        List<Operation> operations, List<Fee> fees,
                        MonetaryValue totalNote, MonetaryValue netOperations,
                        MonetaryValue totalFees, String fileReference, String fileHash,
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

        // Reconstituição do banco: valores já calculados, não recalcular
        if (netOperations != null) {
            this.netOperations = netOperations;
            this.totalFees = totalFees;
            this.operations = operations;
        } else {
            // Parse do PDF: calcular valores derivados
            this.netOperations = operations.stream()
                    .map(Operation::getTotalValue)
                    .reduce(MonetaryValue.zero(), MonetaryValue::add);

            // totalFees vem da soma das taxas extraídas do PDF (fonte de verdade).
            // Fallback para |totalNote - netOperations| apenas se não houver taxas extraídas.
            // A soma das fees é preferível porque a fórmula de fallback falha em notas
            // mistas (compra + venda), onde totalNote é o saldo líquido e não a soma bruta.
            this.totalFees = fees != null && !fees.isEmpty()
                    ? fees.stream().map(Fee::getValue).reduce(MonetaryValue.zero(), MonetaryValue::add)
                    : this.totalNote.subtract(this.netOperations).abs();

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
        return brokerName + "_" + date + ".pdf";
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

    /**
     * Validações de consistência entre os valores calculados e extraídos do PDF.
     *
     * <p>Check 1 — Rateio de taxas: a soma das taxas rateadas nas operações individuais
     * deve bater com o totalFees calculado. Tolerância de R$0,02 para arredondamento.</p>
     *
     * <p>Check 2 — Cross-check do valor líquido: verifica se o valor líquido da nota (totalNote)
     * é consistente com as operações e taxas extraídas. O cálculo considera o sinal das operações
     * (vendas positivas, compras negativas) para funcionar tanto em notas puras quanto mistas.</p>
     *
     * <p>Fórmula: {@code |totalNote| ≈ |signedNet - totalFees|}, onde:
     * <ul>
     *   <li>{@code signedNet = Σ(vendas) - Σ(compras)}</li>
     *   <li>Taxas sempre reduzem o líquido, independente da direção da nota</li>
     * </ul>
     * </p>
     */
    private void validateConsistency() {
        // Check 1: soma das taxas rateadas nas operações ≈ totalFees
        MonetaryValue sumApportioned = operations.stream()
                .map(Operation::getFee)
                .reduce(MonetaryValue.zero(), MonetaryValue::add);
        MonetaryValue diffApportioned = totalFees.subtract(sumApportioned).abs();
        if (diffApportioned.toBigDecimal().doubleValue() > 0.02) {
            throw new TradingNoteValidationException(
                    String.format("Inconsistent fee apportionment: totalFees=%s, sumApportionedFees=%s, diff=%s",
                            totalFees, sumApportioned, diffApportioned));
        }

        // Check 2: |totalNote| ≈ |signedNet - totalFees|
        MonetaryValue signedNet = MonetaryValue.zero();
        for (Operation op : operations) {
            if (op.getType() == OperationType.SELL) {
                signedNet = signedNet.add(op.getTotalValue());
            } else {
                signedNet = signedNet.subtract(op.getTotalValue());
            }
        }
        MonetaryValue expectedLiquid = signedNet.subtract(totalFees);
        MonetaryValue diffLiquid = totalNote.abs().subtract(expectedLiquid.abs()).abs();
        double tolerance = Math.max(totalNote.abs().toBigDecimal().doubleValue() * 0.005, 1.00);
        if (diffLiquid.toBigDecimal().doubleValue() > tolerance) {
            throw new TradingNoteValidationException(
                    String.format("Inconsistent liquid value: totalNote=%s, expected=%s, diff=%s, tolerance=%.2f",
                            totalNote, expectedLiquid, diffLiquid, tolerance));
        }
    }

    /**
     * Rateia o totalFees proporcionalmente entre as operações, usando o volume bruto
     * (soma dos valores absolutos) como base. Isso garante que o rateio funcione
     * corretamente em notas mistas, onde compras e vendas coexistem.
     *
     * <p>Fórmula: {@code fee_operação = |totalValue_operação| × (totalFees / grossVolume)}</p>
     */
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
