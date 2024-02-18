package br.com.investmentmanager.portfolioevent.infrastructure.messagebroker.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PortfolioEventMessage {
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
    private AssetMessage asset;
}
