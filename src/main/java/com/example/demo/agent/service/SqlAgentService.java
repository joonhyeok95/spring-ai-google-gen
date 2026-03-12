package com.example.demo.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.agent.tool.SqlToolService;

import reactor.core.publisher.Flux;

@Service
public class SqlAgentService {

    private final ChatClient chatClient;
    private final SqlToolService sqlTool;

    public SqlAgentService(ChatClient.Builder builder, SqlToolService sqlTool) {
    	this.sqlTool = sqlTool;
        this.chatClient = builder
                .defaultSystem("""
                    당신은 PostgreSQL 전문가입니다. 데이터베이스 이름은 'aidb'이며, DVD 대여 시스템입니다.
                    다음 테이블 정보를 바탕으로 사용자의 질문에 답하는 SQL을 생성하고 실행하세요.
                    
                    - film: 영화 제목, 출시년도 정보
                    - customer: 고객 성명, 이메일
                    - rental: 대여 일시 및 반납 정보
                    - payment: 결제 금액 및 날짜
                    
                    반드시 제공된 도구를 사용해 SQL을 실행한 후, 그 결과를 바탕으로 자연스럽게 답변하세요.
                    
                    [자기 수정 규칙]
                    1. 'executeSqlQuery' 실행 결과 'SQL 에러 발생'이 포함되어 있다면, 에러 메시지를 분석하세요.
                    2. 잘못된 컬럼명이나 테이블명 때문이라면, 스키마를 추측하지 말고 information_schema를 조회하거나 쿼리를 수정하세요.
                    3. 최대 3번까지 수정을 시도할 수 있습니다.
                    4. 최종적으로 성공한 결과만 요약해서 사용자에게 전달하세요.
                    """)
                .build();
    }

	public Flux<String> askStream(String chatId, String message) {
		return chatClient.prompt()
	            .user(message)
	            .advisors(advisor -> advisor.param("chat_memory_conversation_id", chatId))
                .tools(sqlTool) // 위에서 만든 빈 이름
	            .stream() // 핵심: 비동기 스트림 시작
	            .content(); // Flux<String> 타입으로 변환
	}
}