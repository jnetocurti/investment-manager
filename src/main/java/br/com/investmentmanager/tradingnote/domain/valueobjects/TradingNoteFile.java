package br.com.investmentmanager.tradingnote.domain.valueobjects;

import lombok.NonNull;
import lombok.Value;

import java.io.InputStream;

@Value(staticConstructor = "of")
public class TradingNoteFile {
    @NonNull String name;
    @NonNull String contentType;
    @NonNull InputStream content;
}
