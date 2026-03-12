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
    private final Map<String, Sinks.Many<ApprovalRequest>> userSinks = new ConcurrentHashMap<>();
    // 2. 대기 중인 요청 저장소
    private final Map<String, CompletableFuture<Boolean>> pendingApprovals = new ConcurrentHashMap<>();

    // AI가 호출: 승인 요청을 등록하고 UI에 알림
    public CompletableFuture<Boolean> request(String chatId, String query) {
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
        
        // ⭐️ 해당 ChatId에 연결된 Sink를 찾아 데이터 방출
        Sinks.Many<ApprovalRequest> sink = userSinks.get(chatId);
        if (sink != null) {
            Sinks.EmitResult result = sink.tryEmitNext(approvalRequest);
            log.info("📢 [백엔드] ChatId: {} 에게 승인 요청 전송 - ID: {}, 결과: {}", chatId, id, result);
        } else {
            log.warn("⚠️ ChatId: {} 에 대한 활성화된 구독자가 없습니다.", chatId);
            // 구독자가 없으면 즉시 실패 처리하거나 로직에 따라 조절
            future.complete(false);
        }        
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
    public Flux<ApprovalRequest> getApprovalStream(String chatId) {
        // Sinks를 가져오거나 없으면 새로 생성
        Sinks.Many<ApprovalRequest> sink = userSinks.computeIfAbsent(chatId, 
            k -> Sinks.many().multicast().onBackpressureBuffer());

        // 하트비트 데이터 생성 (에러 방지를 위해 ID 위주로 최소화)
        Flux<ApprovalRequest> heartbeat = Flux.interval(java.time.Duration.ofSeconds(15))
                .map(i -> ApprovalRequest.builder()
                        .id("keep-alive")
                        .query("")
                        .message("ping")
                        .requestedAt(LocalDateTime.now())
                        .build());

        return sink.asFlux()
                .doOnSubscribe(sub -> log.info("👀 [SSE] ChatId: {} 구독 시작", chatId))
                .doOnCancel(() -> {
                    log.info("❌ [SSE] ChatId: {} 구독 종료", chatId);
                    // 구독 종료 시 맵에서 제거하면 메모리 누수를 방지할 수 있습니다.
                    // userSinks.remove(chatId); 
                })
                .mergeWith(heartbeat)
                .onErrorResume(e -> {
                    log.error("❌ SSE 스트림 에러 발생: {}", e.getMessage());
                    return Flux.empty();
                });
    }
}
