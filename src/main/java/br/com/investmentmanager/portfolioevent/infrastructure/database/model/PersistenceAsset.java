package br.com.investmentmanager.portfolioevent.infrastructure.database.model;

import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.Data;

@Data
public class PersistenceAsset {
    private String assetCode;
    private AssetType assetType;
}
