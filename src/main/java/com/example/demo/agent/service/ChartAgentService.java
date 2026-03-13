package com.example.demo.agent.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.agent.tool.ChartTool;
import com.example.demo.config.AiProperties;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChartAgentService {

    private final AiProperties prompts;
    private final ChatClient chatClient;
    private final ChartTool chartTool;

	public Flux<String> askStream(String chatId, String message) {
		return chatClient.prompt()
	            .user(message)
	            .system(prompts.getPrompts().getChart())
	            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
                .tools(chartTool) // 위에서 만든 빈 이름
                .toolContext(Map.of("chatId", chatId))
	            .stream() // 핵심: 비동기 스트림 시작
	            .content() // Flux<String> 타입으로 변환
	            .subscribeOn(Schedulers.boundedElastic());
	}
    
}
