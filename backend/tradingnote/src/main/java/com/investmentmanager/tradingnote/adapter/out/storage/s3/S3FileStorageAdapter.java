package com.investmentmanager.tradingnote.adapter.out.storage.s3;

import com.investmentmanager.tradingnote.domain.port.out.FileStoragePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Profile("s3")
public class S3FileStorageAdapter implements FileStoragePort {

    @Override
    public String store(String filename, InputStream content, long size) {
        // TODO: implementar upload para S3
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InputStream retrieve(String fileReference) {
        // TODO: implementar download do S3
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(String fileReference) {
        // TODO: implementar delete no S3
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
