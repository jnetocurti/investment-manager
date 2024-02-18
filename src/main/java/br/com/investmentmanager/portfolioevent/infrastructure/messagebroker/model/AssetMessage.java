package br.com.investmentmanager.portfolioevent.infrastructure.messagebroker.model;

import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.Data;

@Data
public class AssetMessage {
    private String assetCode;
    private AssetType assetType;
}
