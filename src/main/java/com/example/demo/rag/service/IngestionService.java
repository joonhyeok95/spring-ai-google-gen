package com.example.demo.rag.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class IngestionService {

    private final VectorStore vectorStore;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    // 동기 방식일 때 spring-web
    public void ingestPdf(Resource pdfResource) {
        // 1. PDF 읽기
        TikaDocumentReader reader = new TikaDocumentReader(pdfResource);
        List<Document> documents = reader.get();

        // 2. 텍스트 분할 (LLM이 한 번에 읽기 좋게 적절한 크기로 자름)
        // 800토큰 정도씩 자르고, 맥락 유지를 위해 100토큰 정도 겹치게(overlap) 합니다.
        TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 5, 10000, true);
        List<Document> splitDocuments = splitter.apply(documents);

        // 3. 벡터 DB(pgvector)에 저장 
        // 이때 내부적으로 Ollama EmbeddingModel이 사용되어 텍스트가 숫자로 변환됩니다.
        vectorStore.accept(splitDocuments);
        
        System.out.println("✅ PDF 데이터 적재 완료: " + splitDocuments.size() + " 개의 청크 저장됨.");
    }
    // 비동기 방식 webflux
    public Mono<Void> ingestPdfStream(FilePart filePart) {
        // 1. FilePart의 내용을 Resource로 변환 (비동기 처리)
        return DataBufferUtils.join(filePart.content())
            .map(dataBuffer -> {
                // TikaReader는 InputStream이 필요하므로 버퍼에서 읽어옵니다.
                return new InputStreamResource(dataBuffer.asInputStream(true));
            })
            .publishOn(Schedulers.boundedElastic()) // 블로킹 작업(I/O, 임베딩)을 위한 스레드 전환
            .doOnNext(resource -> {
                // 2. PDF 읽기 및 분할
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                List<Document> documents = reader.get();

                TokenTextSplitter splitter = new TokenTextSplitter(800, 100, 5, 10000, true);
                List<Document> splitDocuments = splitter.apply(documents);

                // 3. 벡터 DB 저장 (Gemini 임베딩 발생 지점)
                vectorStore.accept(splitDocuments);
                
                System.out.println("✅ Gemini 적재 완료: " + splitDocuments.size() + " 청크.");
            })
            .then(); // 결과값 없이 완료 신호만 보냄
    }
}