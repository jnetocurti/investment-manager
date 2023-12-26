package br.com.investmentmanager.tradingnote.infrastructure.database.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.infrastructure.database.PersistenceEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
@Document("tradingNotes")
@CompoundIndex(name = "invoice-idx", def = "{'broker': 1, 'invoiceNumber': 1}", unique = true)
public class PersistenceTradingNote extends PersistenceEntity {
    private String broker;
    private String invoiceNumber;
    private MonetaryValue netAmount;
    private MonetaryValue totalAmount;
    private MonetaryValue costs;
    private LocalDate tradingDate;
    private LocalDate liquidateDate;
    private Collection<PersistenceTradingNoteItem> items;
}
