package com.investmentmanager.portfolioevent.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BrokerResolutionInput {

    String name;
    String document;
    String sourceSystem;
    String sourceReferenceId;
    String parserHint;
}
