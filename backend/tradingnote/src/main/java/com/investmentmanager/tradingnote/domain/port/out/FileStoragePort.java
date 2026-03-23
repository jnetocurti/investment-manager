package com.investmentmanager.tradingnote.domain.port.out;

import java.io.InputStream;

public interface FileStoragePort {

    String store(String filename, InputStream content, long size);

    InputStream retrieve(String fileReference);

    void delete(String fileReference);
}
