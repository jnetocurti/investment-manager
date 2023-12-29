package br.com.investmentmanager.tradingnote.integration.storage;

import br.com.investmentmanager.shared.integration.StorageService;
import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNoteFile;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.jeasy.random.FieldPredicates.named;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradingNoteFileRepositoryImplTest {

    @InjectMocks
    private TradingNoteFileRepositoryImpl repository;
    @Mock
    private StorageService storageService;

    @Test
    void save() {
        var easyRandom = new EasyRandom(new EasyRandomParameters().excludeField(named("content")));
        var file = easyRandom.nextObject(TradingNoteFile.class);

        var bucket = easyRandom.nextObject(String.class);
        ReflectionTestUtils.setField(repository, "bucket", bucket);

        repository.save(file);
        verify(storageService, times(1)).storage(file.getName(), file.getContentType(), file.getContent(), bucket);
    }
}