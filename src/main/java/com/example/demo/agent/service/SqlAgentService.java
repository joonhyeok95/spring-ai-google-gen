package com.example.demo.agent.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.agent.tool.SqlToolService;
import com.example.demo.config.AiProperties;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class SqlAgentService {

    private final AiProperties prompts;
    private final ChatClient chatClient;
    private final SqlToolService sqlTool;
    
	public Flux<String> askStream(String chatId, String message) {
//		return Flux.defer(() ->
		return chatClient.prompt()
	            .user(message)
	            .system(prompts.getPrompts().getSql())
	            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
                .tools(sqlTool) // 위에서 만든 빈 이름
                .toolContext(Map.of("chatId", chatId))
	            .stream() // 핵심: 비동기 스트림 시작
	            .content() // Flux<String> 타입으로 변환
	            .subscribeOn(Schedulers.boundedElastic());
//	           )
//			.subscribeOn(Schedulers.boundedElastic()) // 핵심: 블로킹(승인 대기)을 별도 스레드풀에서 수행
//			.publishOn(Schedulers.parallel()); // 3. UI 스트림 응답은 다시 비동기 스레드로 전달
	}
}