package com.investmentmanager.tradingnote.adapter.in.messaging;

import com.investmentmanager.commons.domain.model.OperationType;
import com.investmentmanager.tradingnote.adapter.out.persistence.TradingNoteWithholdingTaxDocument;
import com.investmentmanager.tradingnote.adapter.out.persistence.TradingNoteWithholdingTaxMongoRepository;
import com.investmentmanager.tradingnote.domain.model.TradingNoteCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradingNoteWithholdingTaxListener {

    private static final String TAX_TYPE_IRRF = "IRRF";
    private final TradingNoteWithholdingTaxMongoRepository repository;

    @RabbitListener(queues = "tradingnote.withholding-tax.queue")
    public void onTradingNoteCreated(TradingNoteCreatedEvent event) {
        BigDecimal withholdingTaxesTotal = event.getWithholdingTaxesTotal();
        if (withholdingTaxesTotal == null || withholdingTaxesTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        TradingNoteWithholdingTaxDocument doc = repository
                .findByTradingNoteIdAndTaxType(event.getTradingNoteId(), TAX_TYPE_IRRF)
                .orElseGet(TradingNoteWithholdingTaxDocument::new);

        doc.setTradingNoteId(event.getTradingNoteId());
        doc.setNoteNumber(event.getNoteNumber());
        doc.setBrokerName(event.getBrokerName());
        doc.setBrokerDocument(event.getBrokerDocument());
        doc.setTradingDate(event.getTradingDate());
        doc.setSettlementDate(event.getSettlementDate());
        doc.setTaxType(TAX_TYPE_IRRF);
        doc.setTaxAmount(withholdingTaxesTotal);
        doc.setCurrency(event.getCurrency());
        doc.setUnexpectedForBuyOnly(event.getOperations().stream()
                .noneMatch(op -> OperationType.SELL.name().equals(op.operationType())));

        if (doc.isUnexpectedForBuyOnly()) {
            log.warn("IRRF detectado em nota sem vendas: noteId={}, noteNumber={}", event.getTradingNoteId(), event.getNoteNumber());
        }

        repository.save(doc);
    }
}
