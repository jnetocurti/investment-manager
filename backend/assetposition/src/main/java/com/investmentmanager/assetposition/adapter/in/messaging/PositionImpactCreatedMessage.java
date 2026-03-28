package com.investmentmanager.assetposition.adapter.in.messaging;

import com.investmentmanager.commons.domain.model.AssetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionImpactCreatedMessage {

    private String id;
    private String originalEventId;
    private String ticker;
    private AssetType assetType;
    private String impactType;
    private int sequence;
    private int quantity;
    private String originType;
    private String sourceType;
    private String adjustmentType;
    private Map<String, Object> adjustmentPayload;
    private String brokerName;
    private String brokerDocument;
    private String sourceReferenceId;
    private int schemaVersion;
}
