package br.com.investmentmanager.asset.application.input;

import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.Data;

@Data
public class AssetMessage {
    private String assetCode;
    private AssetType assetType;
}
