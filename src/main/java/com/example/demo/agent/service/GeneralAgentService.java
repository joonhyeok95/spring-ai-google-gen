package com.example.demo.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.agent.tool.GeneralChatTool;
import com.example.demo.config.AiProperties;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GeneralAgentService {

    private final AiProperties prompts;
    private final ChatClient chatClient;
    private final GeneralChatTool generalChatTool;
    
	public Flux<String> askStream(String chatId, String message) {
		return chatClient.prompt()
	            .user(message)
	            .system(prompts.getPrompts().getGeneral())
	            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
	            .tools(generalChatTool)
	            .stream() // 핵심: 비동기 스트림 시작
	            .content(); // Flux<String> 타입으로 변환
	}
}