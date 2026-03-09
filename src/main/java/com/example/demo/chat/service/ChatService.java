package com.example.demo.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

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
}