package com.investmentmanager.tradingnote.adapter.out.parser;

interface NoteExtractor {

    boolean supports(String normalizedText);

    RawNoteData extract(String normalizedText);
}
