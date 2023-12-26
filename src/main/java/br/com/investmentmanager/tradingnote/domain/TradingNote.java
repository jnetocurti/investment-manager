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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static TradingNoteBuilder builder() {
        return new CustomTradingNoteBuilder();
    }

    public MonetaryValue getTotalAmount() {
        return netAmount.add(costs);
    }

    static class CustomTradingNoteBuilder extends TradingNoteBuilder {
        public TradingNote build() {
            var tradingNote = super.build();
            tradingNote.validate();

            if (tradingNote.file != null) {
                tradingNote.registerEvent(new TradingNoteFileCreated(tradingNote.file));
            }

            tradingNote.registerEvent(new TradingNoteCreated(tradingNote));

            return tradingNote;
        }
    }
}
