package com.example.demo.agent.service;

import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.config.AiProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class RagChatService {

    private final AiProperties prompts;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagChatService(@Qualifier("chatClientVector") ChatClient chatClient,
    						VectorStore vectorStore,
    						AiProperties prompts) {
    	this.chatClient = chatClient;
    	this.vectorStore = vectorStore;
    	this.prompts = prompts;
    }
    
    public Flux<String> askStream(String chatId, String message) {
        // 1. 유사 문서 검색 (Blocking 작업을 Flux 흐름으로 변환)
        return Mono.fromCallable(() -> {
                    SearchRequest searchRequest = SearchRequest.builder()
                            .topK(4)
                            .query(message)
                            .build();
                    return vectorStore.similaritySearch(searchRequest);
                })
                .subscribeOn(Schedulers.boundedElastic()) // 검색 작업은 워커 쓰레드에서!
                .flatMapMany(docs -> {
                    // 2. 컨텍스트 조립
                    String context = docs.stream()
                            .map(Document::getText)
                            .collect(Collectors.joining("\n\n"));

                    return chatClient.prompt()
                            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
                            .system(sp -> sp.text(prompts.getPrompts().getRag()).param("context", context))
                            .user(message) 
                            .stream()
                            .content();
                });
    }
}