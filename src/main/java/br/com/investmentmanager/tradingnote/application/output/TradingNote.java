package br.com.investmentmanager.tradingnote.application.output;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Data
public class TradingNote {
    private UUID id;
    private String broker;
    private String invoiceNumber;
    private MonetaryValue netAmount;
    private MonetaryValue totalAmount;
    private MonetaryValue costs;
    private LocalDate tradingDate;
    private LocalDate liquidateDate;
    private Collection<TradingNoteItem> items;
}
