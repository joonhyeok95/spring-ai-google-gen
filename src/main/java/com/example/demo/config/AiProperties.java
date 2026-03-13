package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
	private String apiKey;
    private String model = "gemini-1.5-flash"; // 기본값 설정
    private String defaultSystem;
 
    // 중첩 클래스로 프롬프트 관리
    private final Prompts prompts = new Prompts();

    @Getter
    @Setter
    public static class Prompts {
        private Resource intentRoute;
        private Resource general;
        private Resource rag;
        private Resource sql;
    }
}

