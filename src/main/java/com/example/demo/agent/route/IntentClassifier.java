package com.example.demo.agent.route;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class IntentClassifier {
    private final ChatClient chatClient;

    public IntentClassifier(@Qualifier("chatClientSimple") ChatClient chatClient) {
    	this.chatClient = chatClient.mutate()
                .defaultSystem("사용자의 질문을 분석하여 [DB, RAG, GENERAL] 중 하나로만 대답하세요. " +
                        "데이터베이스 조회(영화, 대여, 고객)는 DB, " +
                        "문서 기반 지식 검색은 RAG, 나머지는 GENERAL입니다.")
                .build();
    }

    public Mono<String> classify(String userQuery) {
    	return Mono.fromCallable(() -> 
	        chatClient.prompt().user(userQuery).call().content().trim().toUpperCase()
	    ).subscribeOn(Schedulers.boundedElastic());
    }
}
