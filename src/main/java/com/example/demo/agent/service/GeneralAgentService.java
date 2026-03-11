package com.example.demo.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.agent.tool.GeneralChatTool;

import reactor.core.publisher.Flux;

@Service
public class GeneralAgentService {

    private final ChatClient chatClient;
    private final GeneralChatTool generalChatTool;
    
	// ChatModel 을 선택할 수 있도록 chatClient 빈을 여러개 생성함.
    //  여기선 vector 설정이 해지된 ChatClient
    public GeneralAgentService(@Qualifier("chatClientSimple") ChatClient chatClient,
    		GeneralChatTool generalChatTool) {
        this.chatClient = chatClient.mutate()
                .defaultSystem("""
                        당신은 친절하고 위트 있는 대화 전문가입니다.
                        1. 사용자와 가벼운 인사를 나누거나 일상적인 대화를 하세요.
                        2. 만약 전문적인 지식이 필요한 질문이라면 'askGeneralQuestion' 도구를 사용하여 정확한 정보를 확인한 뒤 답변하세요.
                        3. 답변은 항상 한국어로 정중하게 하세요.
                        """)
                    .build();
        this.generalChatTool = generalChatTool;
    }
    
	public Flux<String> askStream(String chatId, String message) {
		return chatClient.prompt()
	            .user(message)
	            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
	            .tools(generalChatTool)
	            .stream() // 핵심: 비동기 스트림 시작
	            .content(); // Flux<String> 타입으로 변환
	}
}