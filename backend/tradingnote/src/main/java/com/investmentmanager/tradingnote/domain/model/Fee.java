package com.investmentmanager.tradingnote.domain.model;

import com.investmentmanager.commons.domain.model.MonetaryValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Fee {

    private final String description;
    private final MonetaryValue value;

    @Override
    public String toString() {
        return description + ": " + value;
    }
}
