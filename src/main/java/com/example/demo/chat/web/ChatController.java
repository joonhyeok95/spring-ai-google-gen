package com.example.demo.chat.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.chat.service.ChatService;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam(value = "message") String message) {
        return chatService.generateResponse(message);
    }
}