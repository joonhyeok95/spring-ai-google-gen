package com.example.demo.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    
    public String generateResponse(String message) {
        // 3. Fluent API 방식으로 호출 (call().content() 사용)
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

	public Flux<String> askStream(String chatId, String message) {
		// TODO Auto-generated method stub
		return chatClient.prompt()
	            .user(message)
	            .stream() // 핵심: 비동기 스트림 시작
	            .content(); // Flux<String> 타입으로 변환
	}
}