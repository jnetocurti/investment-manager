package br.com.investmentmanager.portfolioevent.domain.valueobjects;

import br.com.investmentmanager.shared.util.constants.AssetType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Asset {
    private final String assetCode;
    private final AssetType assetType;
}
