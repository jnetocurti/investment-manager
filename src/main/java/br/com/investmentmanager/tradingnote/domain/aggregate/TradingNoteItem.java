package br.com.investmentmanager.tradingnote.domain.aggregate;

import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value(staticConstructor = "of")
public class TradingNoteItem {
    @NonNull String assetCode;
    @NonNull BigDecimal unitPrice;
    @NonNull BigDecimal quantity;
    @NonNull String operation;
    @NonNull BigDecimal netAmount;
    @NonNull BigDecimal totalAmount;
    @NonNull String currency;
}
