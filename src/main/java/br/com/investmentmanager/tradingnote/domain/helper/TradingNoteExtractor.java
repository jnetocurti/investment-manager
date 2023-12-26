package br.com.investmentmanager.tradingnote.domain.helper;

import lombok.NonNull;
import org.javatuples.Sextet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

public interface TradingNoteExtractor {

    TradingNoteExtractor extract(@NonNull byte[] bytes);

    TradingNoteExtractor extract(@NonNull String content);

    String getBroker();

    String getId();

    BigDecimal getTotalAmount();

    LocalDate getTradingDate();

    LocalDate getLiquidateDate();

    Collection<Sextet<String, BigDecimal, BigDecimal, String, BigDecimal, String>> getItems();
}
