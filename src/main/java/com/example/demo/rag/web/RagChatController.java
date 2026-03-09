package com.example.demo.rag.web;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.rag.service.RagChatService;

@RestController
@CrossOrigin(origins = "*") // 테스트용으로 전체 허용
public class RagChatController {

    private final RagChatService ragChatService;

    public RagChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @GetMapping("/ai/rag-chat")
    public String chat(@RequestParam(value = "message") String message) {
        return ragChatService.ask(message);
    }
}