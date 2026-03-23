package com.investmentmanager.commons.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Broker {

    private final String name;
    private final String documentId;

    @Override
    public String toString() {
        return name + " (" + documentId + ")";
    }
}
