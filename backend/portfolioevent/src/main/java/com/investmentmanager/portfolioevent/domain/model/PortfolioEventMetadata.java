package com.investmentmanager.portfolioevent.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortfolioEventMetadata {

    private final String subscriptionTicker;
}
