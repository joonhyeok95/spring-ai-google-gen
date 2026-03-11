package com.example.demo.chat.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.chat.service.ChatService;

import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

//    // web:동기
//    @GetMapping("/ai/chat")
//    public String chat(@RequestParam(value = "message") String message) {
//        return chatService.generateResponse(message);
//    }
    
    // webflux:비동기
    @GetMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam(name = "chatId") String chatId, 
    								@RequestParam(name = "message") String message) {
        return chatService.askStream(chatId, message);
    }
}