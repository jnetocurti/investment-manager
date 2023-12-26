package br.com.investmentmanager.tradingnote.infrastructure.database.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.infrastructure.database.PersistenceEntity;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersistenceTradingNoteItem extends PersistenceEntity {
    private String assetCode;
    private BigDecimal quantity;
    private MonetaryValue unitPrice;
    private MonetaryValue costs;
    private MonetaryValue netAmount;
    private MonetaryValue totalAmount;
    private Operation operation;
}
