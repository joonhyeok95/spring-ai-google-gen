package com.example.demo.agent;


import org.springframework.stereotype.Service;

import com.example.demo.agent.route.IntentClassifier;
import com.example.demo.agent.service.GeneralAgentService;
import com.example.demo.agent.service.RagChatService;
import com.example.demo.agent.service.SqlAgentService;

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
    private final GeneralAgentService chatAgentService; // LLM 검색 서비스
    
    public Flux<String> handle(String chatId, String userQuery) {
        return intentClassifier.classify(userQuery)
            .flatMapMany(intent -> {
            	log.info("AI Routing 처리: {}", intent);
                if (intent.contains("DB")) {
                	return sqlAgentService.askStream(chatId, userQuery);
                }
                // 협업이 필요한 경우 (예: "협업" 키워드나 복합 분석 시)
                if (intent.contains("COLLAB") || userQuery.contains("협업")){
                    return createCollaborationStream(chatId, userQuery);
                }
                if (intent.contains("RAG")) {
                	return ragAgentService.askStream(chatId, userQuery);
                }
                return chatAgentService.askStream(chatId, userQuery);
            });
    }
    private Flux<String> createCollaborationStream(String chatId, String userQuery) {
        // 1. 먼저 SQL 에이전트에게 데이터를 가져오게 합니다. (스트림이 아닌 단일 텍스트로 취합)
        return sqlAgentService.askStream(chatId, userQuery)
            .collectList()
            .map(list -> String.join("", list))
            .flatMapMany(dbResult -> {
                // 2. DB 결과를 프롬프트에 섞어서 General 에이전트에게 넘깁니다.
                String collaborationPrompt = String.format(
                    "다음은 데이터베이스에서 조회한 실제 데이터입니다: [%s]\n" +
                    "이 데이터를 바탕으로 사용자의 요청('%s')에 맞는 매력적인 콘텐츠를 작성해줘.", 
                    dbResult, userQuery
                );
                
                return chatAgentService.askStream(chatId, collaborationPrompt);
            });
    }
}