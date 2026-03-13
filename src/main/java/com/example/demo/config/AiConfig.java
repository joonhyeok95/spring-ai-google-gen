package com.example.demo.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;

import com.google.genai.Client;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {
	private final AiProperties aiProperties;

	@Primary
	@Bean
    public ChatClient chatClientSimple(GoogleGenAiChatModel chatModel, CustomRedisChatMemoryRepository chatMemoryRepository) {
		
        return ChatClient.builder(chatModel)
                .defaultSystem(aiProperties.getDefaultSystem())
                .defaultAdvisors(
                		MessageChatMemoryAdvisor.builder(
                				MessageWindowChatMemory.builder()
                        		.chatMemoryRepository(chatMemoryRepository)
                        		.maxMessages(20)
                                .build()
            					)
                		.conversationId("default")
                        .order(1)
                        .build()
        		)
                .build();
    }
	
	// VectorDB 연결 클라이언트
	@Bean
    public ChatClient chatClientVector(GoogleGenAiChatModel chatModel, VectorStore vectorStore, CustomRedisChatMemoryRepository chatMemoryRepository) {
		
        return ChatClient.builder(chatModel)
                .defaultSystem(aiProperties.getDefaultSystem())
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                		.searchRequest(SearchRequest.builder()
                				.topK(3).build())
                		.build(),
                		MessageChatMemoryAdvisor.builder(
                				MessageWindowChatMemory.builder()
                        		.chatMemoryRepository(chatMemoryRepository)
                        		.maxMessages(20)
                                .build()
            					)
                		.conversationId("default")
                        .order(1)
                        .build()
        		)
                .build();
    }
    
    @Bean
    public Client googleGenAiClient() {
        return Client.builder()
                .apiKey(aiProperties.getApiKey())
                .build();
    }
  
    @Bean
    public InMemoryChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }
    
    @Bean
    public ChatMemory chatMemory(CustomRedisChatMemoryRepository repository) {
        // MessageWindowChatMemory는 최근 N개의 대화만 유지하는 윈도우 방식을 지원합니다.
        return MessageWindowChatMemory.builder()
        		.chatMemoryRepository(repository)
        		.maxMessages(20)
                .build();
    }
    
    @Bean
    @Primary
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
        );    
    }
    
    @Bean
    public EmbeddingModel embeddingModel() {
        // 1. 연결 정보 설정 (API Key 방식)
        GoogleGenAiEmbeddingConnectionDetails connectionDetails = 
            GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(aiProperties.getApiKey())
                .build();

        // 2. 옵션 설정 (TaskType 설정이 중요합니다)
        GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
                .model("gemini-embedding-001") // 공식 추천 모델
                // RAG용 문서 인덱싱이라면 RETRIEVAL_DOCUMENT가 정석입니다.
                .taskType(GoogleGenAiTextEmbeddingOptions.TaskType.RETRIEVAL_DOCUMENT)
                .dimensions(768) // default 3072나 postgresql 이 2000까지만 지원함
                .build();

        // 3. 모델 객체 생성 및 반환
        return new GoogleGenAiTextEmbeddingModel(connectionDetails, options);
    }
}