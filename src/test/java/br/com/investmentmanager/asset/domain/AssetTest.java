package br.com.investmentmanager.asset.domain;

import br.com.investmentmanager.asset.domain.valueobjects.PortfolioEvent;
import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.util.constants.AssetType;
import br.com.investmentmanager.shared.util.constants.Currency;
import br.com.investmentmanager.shared.util.constants.Operation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AssetTest {

    @Test
    void addPurchase() {
        var asset = Asset.builder().assetCode("XPTO11").assetType(AssetType.REAL_ESTATE_FUND).build();

        asset.addPurchase(PortfolioEvent.of("12345", Operation.BUY, BigDecimal.ONE, LocalDate.of(2024, 3, 24), MonetaryValue.of(Currency.BRL, BigDecimal.ONE)));

        assertNotNull(asset);
    }

    @Test
    void addPurchase2() {
        var asset = Asset.builder().assetCode("XPTO11").assetType(AssetType.REAL_ESTATE_FUND).build();

        asset.addPurchase(PortfolioEvent.of("12345", Operation.BUY, BigDecimal.ONE, LocalDate.of(2024, 3, 24), MonetaryValue.of(Currency.BRL, BigDecimal.ONE)));
        asset.addPurchase(PortfolioEvent.of("12345", Operation.BUY, BigDecimal.TEN, LocalDate.of(2024, 3, 24), MonetaryValue.of(Currency.BRL, BigDecimal.TEN)));

        assertNotNull(asset);
    }
}