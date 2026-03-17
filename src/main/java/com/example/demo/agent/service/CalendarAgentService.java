package com.example.demo.agent.service;

import java.time.LocalDate;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class CalendarAgentService {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpToolProvider;

    public CalendarAgentService(ChatClient.Builder chatClientBuilder,
                                ToolCallbackProvider googleCalendarMcpClient) {
    	this.mcpToolProvider = googleCalendarMcpClient;
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(googleCalendarMcpClient) // LLM 이 tool을 찾을 수 있도록
                .build();
    }

    public Flux<String> askStream(String userMessage, String userEmail) {
    	String email = "dkttkemf@gmail.com";
    	return chatClient.prompt()
    			.system(s -> s.text("""
    					당신은 구글 캘린더 전문가입니다.
    					현재 한국 시간은 {current_date}입니다. 
    					모든 일정 작업은 반드시 이 이메일 계정을 기준으로 수행하세요: {email}
                        사용자가 날짜만 말하거나 상대적인 시간(예: 오늘, 내일)을 말하면 이 정보를 바탕으로 계산하세요.
                        일정 등록 시 반드시 'Asia/Seoul' 시간대를 기준으로 처리하세요.
                        """)
                		.param("email", email)
                		.param("current_date", LocalDate.now().toString()))
                .user(userMessage)
                .stream()
                .content();
    }
}