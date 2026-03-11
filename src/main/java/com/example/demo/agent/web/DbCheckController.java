package com.example.demo.agent.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/db-check")
@RequiredArgsConstructor
public class DbCheckController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/test")
    public Mono<String> testConnection() {
        return Mono.fromCallable(() -> {
            try {
                // 단순 숫자 1을 리턴하는 쿼리로 연결 확인
                Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                
                // dvdrental 테이블 데이터가 있는지 실제 확인
                Integer actorCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM actor", Integer.class);
                
                return "✅ DB 연결 성공! (테스트 쿼리 결과: " + result + 
                       ", 전체 배우 수: " + actorCount + ")";
            } catch (Exception e) {
                return "❌ DB 연결 실패: " + e.getMessage();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}