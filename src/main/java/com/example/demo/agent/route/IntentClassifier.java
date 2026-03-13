package com.example.demo.agent.route;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.config.AiProperties;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class IntentClassifier {

    private final ChatClient.Builder builder; // intent 처럼 의도구분에만 사용하는 곳에서 builder 패턴을 활용하자
    private final AiProperties prompts;
	
	@Qualifier("chatClientSimple")
    private ChatClient chatClient;
	
	@PostConstruct
    public void init() {
        this.chatClient = builder
                .defaultSystem(prompts.getPrompts().getIntentRoute())
                .build();
	}
    public Mono<String> classify(String userQuery) {
    	return Mono.fromCallable(() -> 
	        chatClient.prompt().user(userQuery).call().content().trim().toUpperCase()
	    ).subscribeOn(Schedulers.boundedElastic());
    }
}
