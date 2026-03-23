package com.investmentmanager.tradingnote.domain.parser;

import com.investmentmanager.tradingnote.adapter.out.parser.PdfTradingNoteParser;
import com.investmentmanager.tradingnote.domain.model.Operation;
import com.investmentmanager.tradingnote.domain.model.TradingNote;
import com.investmentmanager.tradingnote.domain.port.out.TradingNoteParserPort;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

class BulkParserTest {

    private final TradingNoteParserPort parser = PdfTradingNoteParser.createDefault();

    @Test
    void parseAllPdfs() throws Exception {
        Path baseDir = Path.of("/Users/4058828/Projetos/estudo/investmentmanager/notas_negociacao");
        if (!Files.exists(baseDir)) {
            System.out.println("PDF directory not found, skipping");
            return;
        }

        List<String> passed = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        Set<String> allAssetNames = new TreeSet<>();

        try (Stream<Path> paths = Files.walk(baseDir)) {
            List<Path> pdfs = paths.filter(p -> p.toString().endsWith(".pdf")).sorted().toList();
            System.out.printf("Found %d PDFs%n%n", pdfs.size());

            for (Path pdf : pdfs) {
                String name = baseDir.relativize(pdf).toString();
                try {
                    TradingNote note = parser.parse(new FileInputStream(pdf.toFile()));

                    StringBuilder ops = new StringBuilder();
                    for (Operation op : note.getOperations()) {
                        allAssetNames.add(op.getAssetName());
                        ops.append(String.format("\n      %s '%s' qty=%d unit=%s total=%s fee=%s",
                                op.getType(), op.getAssetName(), op.getQuantity(),
                                op.getUnitPrice(), op.getTotalValue(), op.getFee()));
                    }

                    passed.add(String.format("%s -> OK [%s #%s, %d ops, total=%s, fees=%s]%s",
                            name, note.getBroker().getName(), note.getNoteNumber(),
                            note.getOperations().size(), note.getTotalNote(), note.getTotalFees(), ops));
                } catch (Exception e) {
                    failed.add(name + " -> ERROR: " + e.getMessage());
                }
            }
        }

        System.out.println("=== PASSED (" + passed.size() + ") ===");
        passed.forEach(System.out::println);
        System.out.println("\n=== FAILED (" + failed.size() + ") ===");
        failed.forEach(System.out::println);

        System.out.println("\n=== ALL ASSET NAMES (" + allAssetNames.size() + ") ===");
        allAssetNames.forEach(n -> System.out.println("  " + n));

        System.out.printf("%n%nTotal: %d passed, %d failed out of %d%n",
                passed.size(), failed.size(), passed.size() + failed.size());
    }
}
