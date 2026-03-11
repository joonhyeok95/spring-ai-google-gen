package com.example.demo.agent;


import org.springframework.stereotype.Service;

import com.example.demo.agent.route.IntentClassifier;
import com.example.demo.agent.service.SqlAgentService;
import com.example.demo.chat.service.ChatService;
import com.example.demo.rag.service.RagChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiOrchestrator {
    private final IntentClassifier intentClassifier;
    private final SqlAgentService sqlAgentService; // DB 검색 서비스
    private final RagChatService ragAgentService; // RAG 검색 서비스
    private final ChatService chatAgentService; // LLM 검색 서비스
    
    public Flux<String> handle(String chatId, String userQuery) {
        return intentClassifier.classify(userQuery)
            .flatMapMany(intent -> {
            	log.info("AI Routing 처리: {}", intent);
                if (intent.contains("DB")) return sqlAgentService.askStream(chatId, userQuery);
                if (intent.contains("RAG")) return ragAgentService.askStream(chatId, userQuery);
                return chatAgentService.askStream(chatId, userQuery);
            });
    }
}