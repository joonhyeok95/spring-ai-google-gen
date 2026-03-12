package com.example.demo.agent.tool;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SqlToolService {

    private final JdbcTemplate jdbcTemplate;

    @Tool(description = "PostgreSQL 데이터베이스(aidb)에서 SQL 쿼리를 실행하여 결과를 반환합니다. 오류 발생 시 테이블 구조 힌트를 반환합니다.")
    public String executeSqlQuery(String query) {
        try {
        	List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            return results.toString();
        } catch (Exception e) {
        	return "SQL 에러 발생: " + e.getMessage() + 
                    "\n[도움말] 정확한 컬럼명을 확인하려면 'SELECT column_name FROM information_schema.columns WHERE table_name = '테이블명''을 먼저 실행해보세요.";
        }
    }
}