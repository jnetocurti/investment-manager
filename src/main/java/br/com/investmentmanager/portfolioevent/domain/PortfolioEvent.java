package br.com.investmentmanager.portfolioevent.domain;

import br.com.investmentmanager.portfolioevent.domain.events.PortfolioEventCreated;
import br.com.investmentmanager.portfolioevent.domain.valueobjects.Asset;
import br.com.investmentmanager.shared.domain.AggregateRoot;
import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@ToString
public class PortfolioEvent extends AggregateRoot {
    private final LocalDate eventDate;
    private final LocalDate eventLiquidateDate;
    private final Operation operation;
    private final BigDecimal quantity;
    private final MonetaryValue unitPrice;
    private final MonetaryValue totalAmount;
    private final MonetaryValue netAmount;
    private final MonetaryValue costs;
    private final String broker;
    private final String invoiceNumber;
    private final Asset asset;

    @Builder
    public PortfolioEvent(
            UUID id,
            LocalDate eventDate,
            LocalDate eventLiquidateDate,
            Operation operation,
            BigDecimal quantity,
            MonetaryValue unitPrice,
            MonetaryValue totalAmount,
            MonetaryValue netAmount,
            MonetaryValue costs,
            String broker,
            String invoiceNumber,
            Asset asset
    ) {
        super(id);
        this.eventDate = eventDate;
        this.eventLiquidateDate = eventLiquidateDate;
        this.operation = operation;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.netAmount = netAmount;
        this.costs = costs;
        this.broker = broker;
        this.invoiceNumber = invoiceNumber;
        this.asset = asset;
        validate();

        this.registerEvent(new PortfolioEventCreated(this));
    }

//    public static PortfolioEvent.PortfolioEventBuilder builder() {
//        return new PortfolioEvent.CustomPortfolioEventBuilder();
//    }
//
//    private static class CustomPortfolioEventBuilder extends PortfolioEventBuilder {
//
//        @Override
//        public PortfolioEvent build() {
//            var portfolioEvent = super.build();
//            portfolioEvent.validate();
//
//            portfolioEvent.registerEvent(new PortfolioEventCreated(portfolioEvent));
//
//            return portfolioEvent;
//        }
//    }
}
