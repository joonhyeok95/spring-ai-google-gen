package com.example.demo.agent.tool;


import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.agent.service.ApprovalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlToolService {

    private final JdbcTemplate jdbcTemplate;
    private final ApprovalService approvalService;

    @Tool(description = "PostgreSQL 데이터베이스(aidb)에서 SQL 쿼리를 실행하여 결과를 반환합니다. 오류 발생 시 테이블 구조 힌트를 반환합니다.")
    public String executeSqlQuery(String query, ToolContext toolContext) {
    	log.info("실행할 SQL: {}", query);
    	// ⭐️ ToolContext에서 안전하게 chatId 추출
        String chatId = (String) toolContext.getContext().get("chatId");
        
    	// 1. 위험 쿼리 체크
        if (isDangerous(query)) {
            try {
                // ApprovalService에서 생성된 Future를 60초간 대기 (Blocking call inside tool)
                Boolean approved = approvalService.request(chatId, query)
                        .get(60, TimeUnit.SECONDS);

                if (Boolean.FALSE.equals(approved)) {
                    return "사용자가 쿼리 실행을 거부했습니다. 이 사실을 사용자에게 알리세요.";
                }
            } catch (Exception e) {
                return "승인 요청 후 60초 동안 응답이 없어 중단되었습니다. 다시 시도해 주세요.";
            }
        }
        
        try {
        	log.info("==> 실제 쿼리 실행 중...");
            List<Map<String, Object>> result = jdbcTemplate.queryForList(query);
            log.info("==> 쿼리 실행 완료. 결과 수: {}", result.size());
            return result.toString();
        } catch (Exception e) {
        	return "SQL 에러 발생: " + e.getMessage() + 
                    "\n[도움말] 정확한 컬럼명을 확인하려면 'SELECT column_name FROM information_schema.columns WHERE table_name = '테이블명''을 먼저 실행해보세요.";
        }
    }
    
    @Tool(description = "데이터베이스의 모든 테이블 목록을 가져옵니다.")
    public List<String> listTables() {
        return jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", 
            String.class
        );
    }

    @Tool(description = "특정 테이블의 컬럼 이름과 데이터 타입을 확인합니다.")
    public String describeTable(String tableName) {
        var columns = jdbcTemplate.queryForList(
            "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ?", 
            tableName
        );
        return columns.toString();
    }
    
    private boolean isDangerous(String query) {
        String upper = query.toUpperCase();
        return upper.contains("UPDATE") || upper.contains("DELETE") || 
               upper.contains("DROP") || upper.contains("ALTER") ||
               upper.contains("SELECT");
    }
}