package com.investmentmanager.tradingnote.domain.model;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.commons.domain.model.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operation {

    private String assetName;
    private OperationType type;
    private int quantity;
    private MonetaryValue unitPrice;
    private MonetaryValue totalValue;
    @Setter
    @Builder.Default
    private MonetaryValue fee = MonetaryValue.zero();

    public MonetaryValue getTotalWithFee() {
        return totalValue.add(fee);
    }
}
