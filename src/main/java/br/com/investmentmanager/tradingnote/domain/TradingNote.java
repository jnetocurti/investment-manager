package br.com.investmentmanager.tradingnote.domain;

import br.com.investmentmanager.shared.domain.AggregateRoot;
import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteCreated;
import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import br.com.investmentmanager.tradingnote.domain.validation.constraints.TradingNoteConsistentAmount;
import br.com.investmentmanager.tradingnote.domain.valueobjects.TradingNoteFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@TradingNoteConsistentAmount
public class TradingNote extends AggregateRoot {
    @NotBlank
    private final String broker;
    @NotBlank
    private final String invoiceNumber;
    @NotNull
    private final MonetaryValue netAmount;
    @NotNull
    private final MonetaryValue costs;
    @NotNull
    private final LocalDate tradingDate;
    @NotNull
    private final LocalDate liquidateDate;
    @NotEmpty
    private final List<TradingNoteItem> items;

    private final TradingNoteFile file;

    public MonetaryValue getTotalAmount() {
        return netAmount.add(costs);
    }

    @Builder
    TradingNote(
            UUID id,
            String broker,
            String invoiceNumber,
            MonetaryValue netAmount,
            MonetaryValue costs,
            LocalDate tradingDate,
            LocalDate liquidateDate,
            List<TradingNoteItem> items,
            TradingNoteFile file
    ) {
        super(id);
        this.broker = broker;
        this.invoiceNumber = invoiceNumber;
        this.netAmount = netAmount;
        this.costs = costs;
        this.tradingDate = tradingDate;
        this.liquidateDate = liquidateDate;
        this.items = items;
        this.file = file;
        validate();

        if (this.file != null) {
            this.registerEvent(new TradingNoteFileCreated(this.file));
        }

        this.registerEvent(new TradingNoteCreated(this));
    }
}
