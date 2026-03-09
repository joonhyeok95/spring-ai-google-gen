package com.example.demo.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public String askWithContext(String message) {
        return chatClient.prompt()
                .user(message)
                // 핵심: QuestionAnswerAdvisor가 질문과 관련된 정보를 DB에서 자동으로 찾아 프롬프트에 합쳐줍니다.
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(3).build())
                        .build())
                .call()
                .content();
    }
}