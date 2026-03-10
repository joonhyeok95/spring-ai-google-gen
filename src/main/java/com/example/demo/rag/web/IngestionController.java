package com.example.demo.rag.web;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.rag.service.IngestionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ai/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

//    @PostMapping("/pdf")
//    public String uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
//        ingestionService.ingestPdf(new InputStreamResource(file.getInputStream()));
//        return "파일 업로드 및 벡터화 성공: " + file.getOriginalFilename();
//    }
    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadPdf(@RequestPart("file") FilePart filePartMono) {
    	return ingestionService.ingestPdfStream(filePartMono)
                .then(Mono.just("PDF 데이터 적재가 시작되었습니다. (Gemini 768dim)"));
    }
}