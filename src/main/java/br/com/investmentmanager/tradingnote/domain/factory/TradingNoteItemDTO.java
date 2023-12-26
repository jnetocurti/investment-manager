package br.com.investmentmanager.tradingnote.domain.factory;

import br.com.investmentmanager.shared.util.constants.Currency;
import br.com.investmentmanager.shared.util.constants.Operation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
class TradingNoteItemDTO {
    private Currency currency;
    private String assetCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private Operation operation;
}
