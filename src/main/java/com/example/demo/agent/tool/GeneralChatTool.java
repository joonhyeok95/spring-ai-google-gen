package com.example.demo.agent.tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class GeneralChatTool {

    private final ChatClient chatClient;

    // vector 설정이 해지된 심플한 ChatClient 주입
    public GeneralChatTool(@Qualifier("chatClientSimple") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Tool(description = "일상적인 대화나 일반적인 지식(상식, 역사, 과학 등)에 대해 LLM의 기본 지식을 사용하여 답변을 생성합니다.")
    public Flux<String> askGeneralQuestion(String message) {
        // 도구 실행은 내부적으로 동기(Blocking) 처리를 하여 결과를 텍스트로 반환합니다.
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}