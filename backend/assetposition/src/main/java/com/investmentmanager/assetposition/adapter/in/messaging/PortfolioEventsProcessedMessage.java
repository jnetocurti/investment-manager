package com.investmentmanager.assetposition.adapter.in.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioEventsProcessedMessage {

    private List<AssetKeyMessage> assetKeys;
    private String brokerName;
    private String brokerDocument;
    private String sourceType;
    private String sourceReferenceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetKeyMessage {
        private String assetName;
        private String assetType;
    }
}
