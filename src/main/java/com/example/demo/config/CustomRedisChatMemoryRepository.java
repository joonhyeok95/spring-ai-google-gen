package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomRedisChatMemoryRepository implements ChatMemoryRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String KEY_PREFIX = "chat_memory:";
	@Override
	public List<String> findConversationIds() {
		// chat_memory:* 패턴의 모든 키를 찾아 ID만 추출합니다.
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null) return new ArrayList<>();
        return keys.stream()
                   .map(key -> key.replace(KEY_PREFIX, ""))
                   .collect(Collectors.toList());
	}
	@Override
	public List<Message> findByConversationId(String conversationId) {
		String json = (String) redisTemplate.opsForValue().get(KEY_PREFIX + conversationId);
		System.out.println("DEBUG: [GET] ID: " + conversationId + ", Found: " + (json != null ? "JSON 있음" : "JSON 없음"));
		
	    if (json == null) return new ArrayList<>();
	    try {
	    	List<MessageDto> dtos = objectMapper.readValue(json, new TypeReference<List<MessageDto>>() {});
	        return dtos.stream().map(MessageDto::toModel).collect(Collectors.toList());
	    } catch (Exception e) {
	        System.err.println("DEBUG: [DESERIALIZATION ERROR] " + e.getMessage());
	        return new ArrayList<>();
	    }
	}
	@Override
	public void saveAll(String conversationId, List<Message> messages) {
		try {
	        // 직접 JSON 문자열로 변환하여 저장 (타입 정보 포함)
			// Message 리스트를 DTO 리스트로 변환
	        List<MessageDto> dtos = messages.stream().map(MessageDto::from).toList();
	        String json = objectMapper.writeValueAsString(dtos);
	        redisTemplate.opsForValue().set(KEY_PREFIX + conversationId, json);
	        
	        System.out.println("DEBUG: [SAVE SUCCESS] ID: " + conversationId + " - Size: " + messages.size());
	    } catch (Exception e) {
	        throw new RuntimeException("Redis Save Error", e);
	    }
	}
	@Override
	public void deleteByConversationId(String conversationId) {
		redisTemplate.delete(KEY_PREFIX + conversationId);
	}

	public record MessageDto(String type, String content) {
	    public static MessageDto from(Message message) {
	        return new MessageDto(message.getMessageType().name(), message.getText());
	    }

	    public Message toModel() {
	        // 로그를 찍고 싶다면 switch 밖에서 찍는 것이 가장 깔끔합니다.
	        // System.out.println("DEBUG: Restoring type: " + type);

	        return switch (type) {
	            case "USER" -> new UserMessage(content);
	            case "ASSISTANT" -> new AssistantMessage(content);
	            case "SYSTEM" -> new SystemMessage(content);
	            default -> new UserMessage(content);
	        };
	    }
	}
}