package br.com.investmentmanager.portfolioevent.application.input;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class TradingNoteMessage {
    private UUID id;
    private String broker;
    private String invoiceNumber;
    private MonetaryValue netAmount;
    private MonetaryValue totalAmount;
    private MonetaryValue costs;
    private LocalDate tradingDate;
    private LocalDate liquidateDate;
    private List<TradingNoteItemMessage> items;
}
