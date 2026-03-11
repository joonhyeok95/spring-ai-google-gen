package com.example.demo.agent.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

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

                    String ragPromptText = """
                            아래의 '참고 정보'를 바탕으로 질문에 답하세요. 
                            만약 참고 정보에 답이 없다면 "문서에 해당 내용이 없습니다"라고 답하세요.
                            절대 당신이 원래 알고 있는 지식으로 답하지 마세요.
                            최종 답변은 마크다운을 사용해서 답변해주세요.
                            
                            참고 정보:
                            {context}
                            """;
                    return chatClient.prompt()
                            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
                            .system(sp -> sp.text(ragPromptText).param("context", context))
                            .user(message) 
                            .stream()
                            .content();
                });
    }
}