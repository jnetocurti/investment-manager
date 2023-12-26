package br.com.investmentmanager.tradingnote;

import br.com.investmentmanager.shared.util.constants.Currency;
import br.com.investmentmanager.tradingnote.domain.TradingNote;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.jeasy.random.api.Randomizer;

import java.math.BigDecimal;

import static br.com.investmentmanager.shared.util.constants.Currency.BRL;

public class TradingNoteRandomizer implements Randomizer<TradingNote> {

    @Override
    public TradingNote getRandomValue() {
        return new EasyRandom(new EasyRandomParameters()
                .collectionSizeRange(1, 1)
                .excludeField(FieldPredicates.named("file"))
                .excludeField(FieldPredicates.named("domainEvents"))
                .randomize(BigDecimal.class, () -> BigDecimal.ONE)
                .randomize(Currency.class, () -> BRL))
                .nextObject(TradingNote.class);
    }
}
