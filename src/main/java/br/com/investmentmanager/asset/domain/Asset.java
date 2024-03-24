package br.com.investmentmanager.asset.domain;

import br.com.investmentmanager.asset.domain.valueobjects.PortfolioEvent;
import br.com.investmentmanager.asset.domain.valueobjects.PositionHistory;
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
import java.util.Optional;
import java.util.UUID;

import static java.util.Comparator.comparing;

@Getter
@ToString
public class Asset extends AggregateRoot {
    private final String assetCode;
    private final AssetType assetType;
    private BigDecimal quantity;
    private MonetaryValue averagePurchaseCost;
    private final List<PortfolioEvent> portfolioEvents;
    private final List<PositionHistory> positionHistory;

    @Builder
    public Asset(
            UUID id,
            String assetCode,
            AssetType assetType,
            BigDecimal quantity,
            MonetaryValue averagePurchaseCost,
            List<PortfolioEvent> portfolioEvents,
            List<PositionHistory> positionHistory
    ) {
        super(id);
        this.assetCode = assetCode;
        this.assetType = assetType;
        this.quantity = quantity;
        this.averagePurchaseCost = averagePurchaseCost;
        this.portfolioEvents = Optional.ofNullable(portfolioEvents).orElse(new ArrayList<>());
        this.positionHistory = Optional.ofNullable(positionHistory).orElse(new ArrayList<>());
        validate();
    }

    public void addPurchase(PortfolioEvent portfolioEvent) {
        if (!Operation.BUY.equals(portfolioEvent.getOperation())) {
            throw new IllegalArgumentException();
        }

        if (portfolioEvents.isEmpty()) {
            quantity = portfolioEvent.getQuantity();
            averagePurchaseCost = portfolioEvent.getAveragePurchaseCost();

        } else {
            quantity = quantity.add(portfolioEvent.getQuantity());
            averagePurchaseCost = averagePurchaseCost.add(portfolioEvent.getAveragePurchaseCost());
        }

        addPortfolioEvent(portfolioEvent);
        updatePositionHistory();
    }

    private void addPortfolioEvent(PortfolioEvent portfolioEvent) {
        portfolioEvents.add(portfolioEvent);
        portfolioEvents.sort(comparing(PortfolioEvent::getTradingDate));
    }

    private void updatePositionHistory() {
        positionHistory.clear();

        var quantity = BigDecimal.ZERO;
        var currency = portfolioEvents.get(0).getAveragePurchaseCost().getCurrency();
        var averagePurchaseCost = MonetaryValue.of(currency, BigDecimal.ZERO);

        for (PortfolioEvent e : portfolioEvents) {
            quantity = quantity.add(e.getQuantity());
            averagePurchaseCost = averagePurchaseCost.add(e.getAveragePurchaseCost());

            var current = positionHistory.stream().filter(p -> e.getTradingDate().equals(p.getDate()))
                    .findFirst().orElse(null);

            positionHistory.remove(current);
            positionHistory.add(PositionHistory.of(e.getTradingDate(), quantity, averagePurchaseCost));
        }
    }
}
