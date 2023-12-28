package br.com.investmentmanager.tradingnote.api.controller;

import br.com.investmentmanager.tradingnote.domain.TradingNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("trading-notes")
public class TradingNoteController {

    private final TradingNoteService service;

    @PostMapping("/")
    ResponseEntity<Void> create(@RequestParam("file") MultipartFile file) throws IOException {
        service.create(file.getBytes());

        return ResponseEntity.accepted().build();
    }
}
