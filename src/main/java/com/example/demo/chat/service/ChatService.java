package com.example.demo.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;
    
	// 생성자를 직접 작성하여 주입할 빈의 이름을 명시합니다.
    public ChatService(@Qualifier("chatClientSimple") ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    public String generateResponse(String message) {
        // 3. Fluent API 방식으로 호출 (call().content() 사용)
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

	public Flux<String> askStream(String chatId, String message) {
		return chatClient.prompt()
	            .user(message)
	            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
	            .stream() // 핵심: 비동기 스트림 시작
	            .content(); // Flux<String> 타입으로 변환
	}
}