package br.com.investmentmanager;

import lombok.SneakyThrows;

public final class FileUtils {

    private FileUtils() {
    }

    @SneakyThrows
    public static byte[] loadFile(String file) {
        try (var inputStream = FileUtils.class.getClassLoader().getResourceAsStream(file)) {
            assert inputStream != null;
            return inputStream.readAllBytes();
        }
    }
}
