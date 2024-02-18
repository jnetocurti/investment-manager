package br.com.investmentmanager.tradingnote.infrastructure.messagebroker.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TradingNoteItemMessage {
    private UUID id;
    private String assetCode;
    private BigDecimal quantity;
    private MonetaryValue unitPrice;
    private MonetaryValue costs;
    private MonetaryValue netAmount;
    private MonetaryValue totalAmount;
    private Operation operation;
}
