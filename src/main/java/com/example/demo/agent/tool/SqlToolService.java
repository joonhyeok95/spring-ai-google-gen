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

    @Tool(description = "PostgreSQL 데이터베이스(aidb)에서 SQL 쿼리를 실행하여 결과를 반환합니다.")
    public String executeSqlQuery(String query) {
        try {
        	List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            return results.toString();
        } catch (Exception e) {
            return "SQL 실행 실패: " + e.getMessage();
        }
    }
}