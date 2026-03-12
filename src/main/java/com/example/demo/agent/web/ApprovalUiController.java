package com.example.demo.agent.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.agent.service.ApprovalService;
import com.example.demo.agent.service.dto.ApprovalRequest;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalUiController {
    private final ApprovalService approvalService;

    // 1. UI에서 이 엔드포인트를 열어두면 승인 요청이 올 때마다 알림이 뜸
    @GetMapping(value = "/stream/{chatId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ApprovalRequest> stream(@PathVariable("chatId") String chatId) {
        return approvalService.getApprovalStream(chatId);
    }

    // 2. UI 버튼 클릭 시 호출
    @PostMapping("/{id}/respond")
    public Mono<ResponseEntity<String>> respond(@PathVariable("id") String id, 
    		@RequestParam("approved") boolean approved) {
        return Mono.just(id)
            .publishOn(Schedulers.boundedElastic()) // 승인 처리를 별도 스레드에서 수행
            .map(requestId -> {
                approvalService.handleDecision(requestId, approved);
                return ResponseEntity.ok("Success");
            });
    }
}