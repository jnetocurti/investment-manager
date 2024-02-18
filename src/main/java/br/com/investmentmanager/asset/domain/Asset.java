package br.com.investmentmanager.asset.domain;

import br.com.investmentmanager.asset.domain.valueobjects.PortfolioEvent;
import br.com.investmentmanager.shared.domain.AggregateRoot;
import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.AssetType;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@ToString
public class Asset extends AggregateRoot {
    private final String assetCode;
    private final AssetType assetType;
    private BigDecimal quantity;
    private MonetaryValue averagePurchaseCost;
    @Builder.Default
    private List<PortfolioEvent> portfolioEvents = new ArrayList<>();

    public void addPurchase(PortfolioEvent portfolioEvent) {
        if (!Operation.BUY.equals(portfolioEvent.getOperation())) {
            throw new IllegalArgumentException();
        }

        if (quantity == null) {
            quantity = portfolioEvent.getQuantity();
            averagePurchaseCost = portfolioEvent.getAveragePurchaseCost();
        } else {
            quantity = quantity.add(portfolioEvent.getQuantity());
            averagePurchaseCost = averagePurchaseCost.add(portfolioEvent.getAveragePurchaseCost());
        }

        portfolioEvents.add(portfolioEvent);
    }
}

/**
 * {"eventDate":"2021-12-16",
 * "eventLiquidateDate":"2021-12-20",
 * "operation":"BUY",
 * "quantity":1,
 * "unitPrice":{"currency":"BRL","value":38.93},
 * "totalAmount":{"currency":"BRL","value":38.93665703},
 * "netAmount":{"currency":"BRL","value":38.93},
 * "costs":{"currency":"BRL","value":0.00665703},
 * "broker":"Clear",
 * "invoiceNumber":"19889017",
 * "asset":{
 * "assetCode":"EGIE3",
 * "assetType":"BRAZILIAN_STOCKS"
 * }
 * }
 */