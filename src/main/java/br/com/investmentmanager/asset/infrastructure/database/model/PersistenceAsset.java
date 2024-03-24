package br.com.investmentmanager.asset.infrastructure.database.model;

import br.com.investmentmanager.shared.domain.valueobjects.MonetaryValue;
import br.com.investmentmanager.shared.infrastructure.database.PersistenceEntity;
import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Document("assets")
public class PersistenceAsset extends PersistenceEntity {
    private String assetCode;
    private AssetType assetType;
    private BigDecimal quantity;
    private MonetaryValue averagePurchaseCost;
    private List<PersistencePortfolioEvent> portfolioEvents;
    private List<PersistencePositionHistory> positionHistory;
}
