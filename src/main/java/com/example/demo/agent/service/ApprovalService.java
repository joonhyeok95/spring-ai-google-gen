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
        // 1. 기존에 해당 chatId로 관리되던 Sink가 있다면 명시적으로 종료 처리
        Sinks.Many<ApprovalRequest> oldSink = userSinks.get(chatId);
        if (oldSink != null) {
            log.info("🧹 ChatId: {} 의 기존 세션을 정리하고 새로운 연결을 준비합니다.", chatId);
            // 기존 스트림을 닫아 브라우저/서버 자원을 정리
            oldSink.tryEmitComplete(); 
        }

        // 2. 무조건 새로운 Sink를 생성하여 맵에 저장 (기존 유령 Sink 덮어쓰기)
        Sinks.Many<ApprovalRequest> newSink = Sinks.many().multicast().onBackpressureBuffer();
        userSinks.put(chatId, newSink);

        // 3. 하트비트 설정 (연결 유지용)
        Flux<ApprovalRequest> heartbeat = Flux.interval(java.time.Duration.ofSeconds(15))
                .map(i -> ApprovalRequest.builder()
                        .id("keep-alive")
                        .query("")
                        .message("ping")
                        .requestedAt(LocalDateTime.now())
                        .build());

        // 4. 새로운 Sink의 Flux 반환
        return newSink.asFlux()
                .doOnSubscribe(sub -> log.info("👀 [SSE] ChatId: {} 새로운 구독 시작 (신규 Sink 할당)", chatId))
                .doOnCancel(() -> {
                    log.info("❌ [SSE] ChatId: {} 구독 취소", chatId);
                    // 구독 취소 시 해당 chatId 전용 Sink를 맵에서 제거하여 메모리 누수 방지
                    userSinks.remove(chatId);
                })
                .mergeWith(heartbeat)
                .onErrorResume(e -> {
                    log.error("❌ SSE 스트림 에러 발생 (ChatId: {}): {}", chatId, e.getMessage());
                    userSinks.remove(chatId); // 에러 시에도 정리
                    return Flux.empty();
                });
    }
}
