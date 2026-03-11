//package com.example.demo.config;
//
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Description;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//@Configuration
//public class SqlAgentConfig {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public SqlAgentConfig(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    @Bean
//    @Description("PostgreSQL 데이터베이스에서 SQL 쿼리를 실행하여 결과를 반환합니다.")
//    public Function<SqlRequest, String> executeSqlQuery() {
//        return request -> {
//            try {
//                // 주의: 실제 운영 환경에서는 Read-Only 계정 사용 및 쿼리 검증 필수
//                List<Map<String, Object>> results = jdbcTemplate.queryForList(request.query());
//                return results.toString();
//            } catch (Exception e) {
//                return "쿼리 실행 중 오류 발생: " + e.getMessage();
//            }
//        };
//    }
//
//    public record SqlRequest(String query) {}
//}