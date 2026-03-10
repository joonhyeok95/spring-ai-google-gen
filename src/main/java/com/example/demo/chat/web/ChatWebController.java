package com.example.demo.chat.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatWebController {
	
    private final ChatService chatService;

    @GetMapping
    public String chatPage() {
        return "stream-chat"; // src/main/resources/templates/chat.html 파일을 찾습니다.
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody // HTML이 아닌 데이터를 반환
    public Flux<String> chatStream(@RequestParam(name = "chatId") String chatId, 
    								@RequestParam(name = "message") String message) {
        return chatService.askStream(chatId, message);
    }
}
