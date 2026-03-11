package com.example.demo.rag.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.agent.service.RagChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Controller
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagChatWebController {

    private final RagChatService RagChatService;

    @GetMapping
    public String chatPage() {
        return "stream-rag-chat"; // src/main/resources/templates/chat.html 파일을 찾습니다.
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody // HTML이 아닌 데이터를 반환
    public Flux<String> chatStream(@RequestParam(name = "chatId") String chatId, 
    								@RequestParam(name = "message") String message) {
    	log.info("rag stream controller: chatId:{}", chatId);
        return RagChatService.askStream(chatId, message);
    }
}
