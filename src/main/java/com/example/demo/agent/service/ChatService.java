package com.example.demo.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;
    
	// ChatModel 을 선택할 수 있도록 chatClient 빈을 여러개 생성함.
    //  여기선 vector 설정이 해지된 ChatClient
    public ChatService(@Qualifier("chatClientSimple") ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
	public Flux<String> askStream(String chatId, String message) {
		return chatClient.prompt()
	            .user(message)
	            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
	            .stream() // 핵심: 비동기 스트림 시작
	            .content(); // Flux<String> 타입으로 변환
	}
}