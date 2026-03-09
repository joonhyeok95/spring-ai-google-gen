package com.example.demo.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import com.google.genai.Client;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {
	private final AiProperties aiProperties;
	
	@Bean
    public ChatClient chatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(aiProperties.getDefaultSystem())
                .build();
    }
    
    @Bean
    public Client googleGenAiClient() {
        return Client.builder()
                .apiKey(aiProperties.getApiKey())
                .build();
    }
    
    @Bean
    public GoogleGenAiChatModel googleGenAiChatModel(
            Client googleGenAiClient,
            ObjectProvider<ToolCallingManager> toolCallingManager,
            ObjectProvider<RetryTemplate> retryTemplate,
            ObjectProvider<ObservationRegistry> observationRegistry) {
        // 모델 옵션 설정
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(aiProperties.getModel())
                .temperature(0.7) // 창의성 조절 (0.0 ~ 2.0)
                .build();
        // 모델 생성
        return new GoogleGenAiChatModel(
                googleGenAiClient, 
                options, 
                toolCallingManager.getIfAvailable(() -> null), // Tool 호출 관리
                retryTemplate.getIfAvailable(RetryTemplate::new), // 재시도 로직
                observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP) // 모니터링
        );    }
}