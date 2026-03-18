package com.example.demo.agent.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class NotionAgentService {

    private final ChatClient chatClient;

    public NotionAgentService(ChatClient.Builder chatClientBuilder,
				            ToolCallbackProvider notionMcpClient) {
    	
				this.chatClient = chatClientBuilder
				.defaultToolCallbacks(notionMcpClient) // LLM 이 tool을 찾을 수 있도록
                .defaultSystem("""
                		당신은 노션 관리 에이전트입니다
                		user_id 값은 3264d4d8-7956-810a-8939-0027b6fd1837 이거야.
                		추가로, '노션 AI 전용공간'이라는 이름의 페이지를 먼저 검색(search)한 뒤, 그곳에 페이지를 생성하세요.
                		사용자 식별 정보 없이 페이지 내용만 생성하세요.
                		
                		당신은 노션 시인입니다. 다음 순서를 엄격히 지키세요:
				        1. 'search' 도구로 '노션 AI 전용공간' 페이지 ID를 찾는다.
				        2. 'create_page' 도구로 해당 공간 아래에 제목을 정해 페이지를 만든다.
				        3. 위에서 생성된 'new_page_id'를 사용하여 'append_block_children' 도구로 시 본문을 작성한다.
				        모든 단계가 끝나기 전에는 답변을 종료하지 마세요.
                		""")
				.build();
    }

    public Flux<String> process(String message, String userEmail) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
