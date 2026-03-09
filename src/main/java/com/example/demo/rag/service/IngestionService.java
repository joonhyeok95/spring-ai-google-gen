package com.example.demo.rag.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

    private final VectorStore vectorStore;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestPdf(Resource pdfResource) {
        // 1. PDF 읽기
        TikaDocumentReader reader = new TikaDocumentReader(pdfResource);
        List<Document> documents = reader.get();

        // 2. 텍스트 분할 (LLM이 한 번에 읽기 좋게 적절한 크기로 자름)
        // 800토큰 정도씩 자르고, 맥락 유지를 위해 100토큰 정도 겹치게(overlap) 합니다.
        TokenTextSplitter splitter = new TokenTextSplitter(500, 50, 5, 10000, true);
        List<Document> splitDocuments = splitter.apply(documents);

        // 3. 벡터 DB(pgvector)에 저장 
        // 이때 내부적으로 Ollama EmbeddingModel이 사용되어 텍스트가 숫자로 변환됩니다.
        vectorStore.accept(splitDocuments);
        
        System.out.println("✅ PDF 데이터 적재 완료: " + splitDocuments.size() + " 개의 청크 저장됨.");
    }
}