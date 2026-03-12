package com.example.demo.agent.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.demo.agent.service.dto.ApprovalRequest;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
public class ApprovalService {
    // 1. UI로 실시간 알림을 보내기 위한 통로 (SSE)
    private final Sinks.Many<ApprovalRequest> sink = Sinks.many().multicast().onBackpressureBuffer();
    // 2. 대기 중인 요청 저장소
    private final Map<String, CompletableFuture<Boolean>> pendingApprovals = new ConcurrentHashMap<>();

    // AI가 호출: 승인 요청을 등록하고 UI에 알림
    public CompletableFuture<Boolean> request(String query) {
        String id = UUID.randomUUID().toString().substring(0, 8);// 60초 후 자동 만료되는 로직 추가 (Optional)
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>()
                .orTimeout(60, TimeUnit.SECONDS);
        pendingApprovals.put(id, future);
     // 1. DTO 객체 생성
        ApprovalRequest approvalRequest = ApprovalRequest.builder()
        		.id(id)
        		.query(query)
        		.message("쿼리가 생성되어 사용자 승인을 기다립니다.")
        		.requestedAt(LocalDateTime.now())
        		.build();
        // UI에 보낼 객체 생성 및 방출
        Sinks.EmitResult result = sink.tryEmitNext(approvalRequest);
        log.info("📢 [백엔드] 승인 요청 방출 - ID: {}, 결과: {}", id, result);
        
        return future;
    }

    // UI 버튼 클릭 시 호출: 승인 또는 취소
    public void handleDecision(String id, boolean approved) {
    	log.info("📥 [백엔드] 결정 수신 - ID: {}, 승인여부: {}", id, approved);
    	CompletableFuture<Boolean> future = pendingApprovals.get(id);
	    if (future != null) {
	        future.complete(approved);
	        pendingApprovals.remove(id); // 결정 완료 후 삭제
	    } else {
	        log.warn("⚠️ 이미 만료되었거나 존재하지 않는 승인 ID입니다: {}", id);
	    }
    }

    // UI에서 구독할 스트림 (SSE 엔드포인트용)
    public Flux<ApprovalRequest> getApprovalStream() {
    	return sink.asFlux()
            .doOnSubscribe(sub -> log.info("👀 새로운 UI 구독자 연결됨"))
            .doOnCancel(() -> log.info("❌ UI 구독 연결 종료"))
            .share(); // 여러 탭에서 동시에 볼 수 있게 공유
    }
}
