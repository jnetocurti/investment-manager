package br.com.investmentmanager.portfolioevent.infrastructure.database.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.infrastructure.database.PersistenceEntity;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Document("portfolioEvents")
//@CompoundIndex(name = "invoice-idx", def = "{'broker': 1, 'invoiceNumber': 1}", unique = true)
public class PersistencePortfolioEvent extends PersistenceEntity {
    private LocalDate eventDate;
    private LocalDate eventLiquidateDate;
    private Operation operation;
    private BigDecimal quantity;
    private MonetaryValue unitPrice;
    private MonetaryValue totalAmount;
    private MonetaryValue netAmount;
    private MonetaryValue costs;
    private String broker;
    private String invoiceNumber;
    private PersistenceAsset asset;
}
