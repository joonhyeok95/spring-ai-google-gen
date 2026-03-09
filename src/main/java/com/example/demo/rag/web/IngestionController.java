package com.example.demo.rag.web;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.rag.service.IngestionService;

@RestController
@RequestMapping("/ai/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/pdf")
    public String uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        ingestionService.ingestPdf(new InputStreamResource(file.getInputStream()));
        return "파일 업로드 및 벡터화 성공: " + file.getOriginalFilename();
    }
}