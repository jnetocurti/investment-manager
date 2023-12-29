package br.com.investmentmanager.tradingnote.integration.storage;

import br.com.investmentmanager.shared.integration.StorageService;
import br.com.investmentmanager.tradingnote.domain.aggregate.TradingNoteFile;
import br.com.investmentmanager.tradingnote.domain.repository.TradingNoteFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradingNoteFileRepositoryImpl implements TradingNoteFileRepository {

    private final StorageService storageService;

    @Value("${storage.bucket}")
    private String bucket;

    @Override
    public void save(TradingNoteFile file) {
        storageService.storage(file.getName(), file.getContentType(), file.getContent(), bucket);
    }
}
