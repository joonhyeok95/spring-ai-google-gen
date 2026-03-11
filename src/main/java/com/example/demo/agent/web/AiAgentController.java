package com.example.demo.agent.web;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.agent.AiOrchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAgentController {

    private final AiOrchestrator orchestrator;

    /**
     * 질문을 던지면 의도를 파악한 뒤(DB/RAG/General), 
     * 해당 에이전트의 답변을 실시간 스트리밍으로 반환합니다.
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam(value = "message") String message) {
        String chatId = "test";
        return orchestrator.handle(chatId, message)
                .doOnNext(token -> log.debug("발생 토큰: {}", token))
                .doOnError(e -> log.error("스트리밍 중 에러 발생", e));
    }
}