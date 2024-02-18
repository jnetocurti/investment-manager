package br.com.investmentmanager.asset.application.usecases;

import br.com.investmentmanager.asset.application.input.PortfolioEventMessage;
import br.com.investmentmanager.asset.domain.AssetService;
import br.com.investmentmanager.asset.domain.valueobjects.PortfolioEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddAssetPurchase {

    private final AssetService service;

    public void execute(PortfolioEventMessage event) {

        var asset = service.getOrCreate(event.getAsset().getAssetCode(), event.getAsset().getAssetType());

        asset.addPurchase(PortfolioEvent.builder()
                .quantity(event.getQuantity())
                .operation(event.getOperation())
                .averagePurchaseCost(event.getTotalAmount())
                .build());

        service.save(asset);
    }
}
